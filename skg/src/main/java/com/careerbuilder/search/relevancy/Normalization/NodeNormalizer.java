package com.careerbuilder.search.relevancy.Normalization;

import com.careerbuilder.search.relevancy.Generation.FacetFieldAdapter;
import com.careerbuilder.search.relevancy.Models.RequestNode;
import com.careerbuilder.search.relevancy.Models.ResponseNode;
import com.careerbuilder.search.relevancy.NodeContext;
import com.careerbuilder.search.relevancy.RecursionOp;
import com.careerbuilder.search.relevancy.Runnable.FacetRunner;
import com.careerbuilder.search.relevancy.ThreadPool.ThreadPool;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Sort;
import org.apache.solr.common.util.SimpleOrderedMap;

import java.io.IOException;
import java.util.LinkedList;

public class NodeNormalizer implements RecursionOp {

    private static final int DEFAULT_NORM_LIMIT = 100;

    public ResponseNode [] transform(NodeContext context, RequestNode [] requests, ResponseNode [] responses) throws IOException {
        FacetFieldAdapter [] adapters = new FacetFieldAdapter[requests.length];
        FacetRunner [] runners = buildRunners(context, requests, adapters);
        ThreadPool.multiplex(runners);
        ThreadPool.demultiplex(runners);
        normalizeRequests(requests, adapters, runners);
        return null;
    }

    private void normalizeRequests(RequestNode[] requests, FacetFieldAdapter[] adapters, FacetRunner[] runners) {
        int n = 0;
        for(int i = 0; i < requests.length; ++i) {
            if(requests[i].values != null && adapters[i].hasExtension())
            {
                LinkedList<String> normalizedStrings = new LinkedList<>();
                LinkedList<SimpleOrderedMap<String>> normalizedMaps= new LinkedList<>();
                for(int k = 0; k < requests[i].values.length; ++k)
                {
                    if(!populateNorms(adapters[i], runners[n], requests[i].values[k], normalizedStrings, normalizedMaps)) {
                        normalizedStrings.add(requests[i].values[k]);
                        normalizedMaps.add(null);
                    }
                    ++n;
                }
                requests[i].normalizedValues = normalizedMaps;
                requests[i].values = normalizedStrings.toArray(new String[normalizedStrings.size()]);
            }
        }
    }

    private boolean populateNorms(FacetFieldAdapter adapter,
                                  FacetRunner runner,
                                  String inputValue,
                                  LinkedList<String> normalizedStrings,
                                  LinkedList<SimpleOrderedMap<String>> normalizedMaps) {
        int j = 0;
        while(j < runner.buckets.size()){
            String normString = adapter.getStringValue(runner.buckets.get(j));
            if(normString.toLowerCase().contains(inputValue.toLowerCase())) {
                normalizedStrings.add(normString);
                normalizedMaps.add(adapter.getMapValue(runner.buckets.get(j)));
            }
            ++j;
        }
        return j != 0;
    }

    public FacetRunner [] buildRunners(NodeContext context, RequestNode [] requests, FacetFieldAdapter [] adapters) throws IOException
    {
        int numValues = buildAdapters(context, requests, adapters);
        FacetRunner [] runners = new FacetRunner[numValues];
        int n = 0;
        for(int i = 0; i < requests.length; ++i) {
            if(requests[i].values != null && adapters[i].hasExtension()) {
                for (int k = 0; k < requests[i].values.length; ++k) {
                    // populate required docListAndSet once and only if necessary
                    if (context.queryDomainList == null) {
                        context.queryDomainList =
                                context.req.getSearcher().getDocListAndSet(new MatchAllDocsQuery(),
                                        context.queryDomain, Sort.INDEXORDER, 0, 0);
                    }
                    String facetQuery = buildFacetQuery(adapters[i].baseField, requests[i].values[k].toLowerCase());
                    runners[n++] = new FacetRunner(context, facetQuery, adapters[i].field, DEFAULT_NORM_LIMIT);
                }
            }
        }
        return runners;
    }

    private int buildAdapters(NodeContext context, RequestNode[] requests, FacetFieldAdapter[] adapters) {
        int numValues = 0;
        for(int i = 0; i < requests.length; ++i){
            adapters[i] = new FacetFieldAdapter(context, requests[i].type);
            numValues += requests[i].values == null || !adapters[i].hasExtension() ? 0 : requests[i].values.length;
        }
        return numValues;
    }

    private String buildFacetQuery(String field, String inputValue) {
        if(inputValue == null)
            return field + ":*";
        return field + ":\"" + inputValue + "\"";
    }
}
