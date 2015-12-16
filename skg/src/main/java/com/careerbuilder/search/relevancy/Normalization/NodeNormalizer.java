package com.careerbuilder.search.relevancy.Normalization;

import com.careerbuilder.search.relevancy.Generation.FacetFieldAdapter;
import com.careerbuilder.search.relevancy.Models.RequestNode;
import com.careerbuilder.search.relevancy.Models.ResponseNode;
import com.careerbuilder.search.relevancy.NodeContext;
import com.careerbuilder.search.relevancy.RecursionOp;
import com.careerbuilder.search.relevancy.Runnable.FacetRunner;
import com.careerbuilder.search.relevancy.ThreadPool.ThreadPool;
import com.careerbuilder.search.relevancy.utility.MapUtility;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Sort;
import org.apache.solr.common.util.SimpleOrderedMap;

import java.io.IOException;
import java.util.LinkedList;

public class NodeNormalizer implements RecursionOp {

    // this is the map index whose value should match the passed in value
    private static final String STRING_VALUE_IDENTIFIER = "name";
    private static final int DEFAULT_NORM_LIMIT = 100;

    public ResponseNode [] transform(NodeContext context, RequestNode [] requests, ResponseNode [] responses) throws IOException {
        FacetFieldAdapter [] adapters = buildAdapters(context, requests);
        FacetRunner [] runners = buildRunners(context, requests, adapters);
        ThreadPool.multiplex(runners);
        ThreadPool.demultiplex(runners);
        normalizeRequests(requests, adapters, runners);
        return null;
    }

    public void normalizeRequests(RequestNode[] requests, FacetFieldAdapter[] adapters, FacetRunner[] runners) {
        int runnerStartIndex = 0;
        for(int i = 0; i < requests.length; ++i) {
            int length = requests[i].values == null ? 0 : requests[i].values.length;
            normalizeSingleRequest(requests[i], adapters[i], runners, runnerStartIndex);
            runnerStartIndex += length;
        }
    }

    private void normalizeSingleRequest(RequestNode request, FacetFieldAdapter adapter, FacetRunner[] runners, int requestStartIndex) {
        if(request.values != null && adapter.hasExtension())
        {
            LinkedList<String> normalizedStrings = new LinkedList<>();
            LinkedList<SimpleOrderedMap<String>> normalizedMaps= new LinkedList<>();
            for(int k = 0; k < request.values.length; ++k)
            {
                if(!populateNorms(adapter, runners[k + requestStartIndex], request.values[k], normalizedStrings, normalizedMaps)) {
                    normalizedStrings.add(request.values[k]);
                    normalizedMaps.add(null);
                }
            }
            request.normalizedValues = normalizedMaps;
            request.values = normalizedStrings.toArray(new String[normalizedStrings.size()]);
        }
    }

    private boolean populateNorms(FacetFieldAdapter adapter,
                                  FacetRunner runner,
                                  String requestValue,
                                  LinkedList<String> normalizedStrings,
                                  LinkedList<SimpleOrderedMap<String>> normalizedMaps) {
        for(int j = 0; j < runner.buckets.size(); ++j){
            SimpleOrderedMap<String> facetResult = adapter.getMapValue(runner.buckets.get(j));
            if(MapUtility.mapContainsValue(requestValue.toLowerCase(), facetResult)) {
                normalizedStrings.add(adapter.getStringValue(runner.buckets.get(j)));
                normalizedMaps.add(adapter.getMapValue(runner.buckets.get(j)));
                return true;
            }
        }
        return false;
    }

    private FacetRunner [] buildRunners(NodeContext context, RequestNode [] requests, FacetFieldAdapter [] adapters) throws IOException
    {
        LinkedList<FacetRunner> runners = new LinkedList<>();
        for(int i = 0; i < requests.length; ++i) {
                buildRequestRunners(context, requests[i], adapters[i], runners);
            }
        return runners.toArray(new FacetRunner[runners.size()]);
    }

    private void buildRequestRunners(NodeContext context, RequestNode request, FacetFieldAdapter adapter, LinkedList<FacetRunner> runners) throws IOException {
        if(request.values != null && adapter.hasExtension()) {
            for (int k = 0; k < request.values.length; ++k) {
                // load required docListAndSet once and only if necessary
                if (context.queryDomainList == null) {
                    context.queryDomainList =
                            context.req.getSearcher().getDocListAndSet(new MatchAllDocsQuery(),
                                    context.queryDomain, Sort.INDEXORDER, 0, 0);
                }
                String facetQuery = buildFacetQuery(adapter.baseField, request.values[k].toLowerCase());
                runners.add(new FacetRunner(context, facetQuery, adapter.field, DEFAULT_NORM_LIMIT));
            }
        }
    }

    private FacetFieldAdapter[] buildAdapters(NodeContext context, RequestNode[] requests) {
        FacetFieldAdapter [] adapters = new FacetFieldAdapter[requests.length];
        for(int i = 0; i < requests.length; ++i){
            adapters[i] = new FacetFieldAdapter(context, requests[i].type);
        }
        return adapters;
    }

    private String buildFacetQuery(String field, String inputValue) {
        if(inputValue == null)
            return field + ":*";
        return field + ":\"" + inputValue + "\"";
    }
}
