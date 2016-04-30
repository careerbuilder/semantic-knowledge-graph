/**Copyright 2015-2016 CareerBuilder, LLC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
**/

package com.careerbuilder.search.relevancy.normalization;

import com.careerbuilder.search.relevancy.generation.FacetFieldAdapter;
import com.careerbuilder.search.relevancy.model.RequestNode;
import com.careerbuilder.search.relevancy.model.ResponseNode;
import com.careerbuilder.search.relevancy.NodeContext;
import com.careerbuilder.search.relevancy.RecursionOp;
import com.careerbuilder.search.relevancy.runnable.FacetRunner;
import com.careerbuilder.search.relevancy.runnable.Waitable;
import com.careerbuilder.search.relevancy.threadpool.ThreadPool;
import com.careerbuilder.search.relevancy.utility.MapUtility;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Sort;
import org.apache.solr.common.util.SimpleOrderedMap;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Future;

public class NodeNormalizer implements RecursionOp {

    private static final int DEFAULT_NORM_LIMIT = 100;

    public ResponseNode [] transform(NodeContext context, RequestNode [] requests, ResponseNode [] responses) throws IOException {
        Map<String, List<FacetRunner>> runners = buildRunners(context, requests);
        List<Future<Waitable>> futures = ThreadPool.multiplex(runners.values().stream()
                .flatMap(l->l.stream()).toArray(FacetRunner[]::new));
        ThreadPool.demultiplex(futures);
        normalizeRequests(requests, runners);
        return null;
    }

    public void normalizeRequests(RequestNode[] requests, Map<String, List<FacetRunner>> runners) {
        for(int i = 0; i < requests.length; ++i) {
            normalizeSingleRequest(requests[i], runners.get(requests[i].type));
        }
    }

    private void normalizeSingleRequest(RequestNode request, List<FacetRunner> runners) {
        if(runners.size() > 0 &&  request.values != null && runners.get(0).adapter.hasExtension())
        {
            LinkedList<String> normalizedStrings = new LinkedList<>();
            LinkedList<SimpleOrderedMap<String>> normalizedMaps= new LinkedList<>();
            for(FacetRunner runner : runners)
            {
                populateNorms(runner, request.values[runner.index], normalizedStrings, normalizedMaps);
            }
            request.normalizedValues = normalizedMaps;
            request.values = normalizedStrings.toArray(new String[normalizedStrings.size()]);
        }
    }

    private void populateNorms(FacetRunner runner,
                               String requestValue,
                               LinkedList<String> normalizedStrings,
                               LinkedList<SimpleOrderedMap<String>> normalizedMaps) {
        for(SimpleOrderedMap<Object> bucket : runner.buckets)
        {
            SimpleOrderedMap<String> facetResult = runner.adapter.getMapValue(bucket);
            if(MapUtility.mapContainsValue(requestValue.toLowerCase(), facetResult))
            {
                normalizedStrings.add(runner.adapter.getStringValue(bucket));
                normalizedMaps.add(runner.adapter.getMapValue(bucket));
                return;
            }
        }
        normalizedStrings.add(requestValue);
        normalizedMaps.add(null);
    }

    private Map<String, List<FacetRunner>> buildRunners(NodeContext context, RequestNode [] requests) throws IOException
    {
        Map<String, List<FacetRunner>> runners = new TreeMap<>();
        for(int i = 0; i < requests.length; ++i)
        {
             runners.put(requests[i].type, buildRequestRunners(context, requests[i]));
        }
        return runners;
    }

    private List<FacetRunner> buildRequestRunners(NodeContext context, RequestNode request) throws IOException {
        List<FacetRunner> runners = new LinkedList<>();
        FacetFieldAdapter adapter = new FacetFieldAdapter(context, request.type);
        if(request.values != null && adapter.hasExtension())
        {
            for (int k = 0; k < request.values.length; ++k)
            {
                // load required docListAndSet once and only if necessary
                if (context.queryDomainList == null)
                {
                    context.queryDomainList =
                            context.req.getSearcher().getDocListAndSet(new MatchAllDocsQuery(),
                                    context.queryDomain, Sort.INDEXORDER, 0, 0);
                }
                String facetQuery = buildFacetQuery(adapter.baseField, request.values[k].toLowerCase());
                runners.add(new FacetRunner(context, adapter, facetQuery, adapter.field, k, DEFAULT_NORM_LIMIT));
            }
        }
        return runners;
    }

    private String buildFacetQuery(String field, String inputValue) {
        if(inputValue == null)
            return field + ":*";
        return field + ":\"" + inputValue + "\"";
    }
}
