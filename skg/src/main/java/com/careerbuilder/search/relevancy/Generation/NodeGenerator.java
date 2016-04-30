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

package com.careerbuilder.search.relevancy.generation;

import com.careerbuilder.search.relevancy.model.RequestNode;
import com.careerbuilder.search.relevancy.model.ResponseNode;
import com.careerbuilder.search.relevancy.model.ResponseValue;
import com.careerbuilder.search.relevancy.NodeContext;
import com.careerbuilder.search.relevancy.RecursionOp;
import com.careerbuilder.search.relevancy.runnable.FacetRunner;
import com.careerbuilder.search.relevancy.runnable.Waitable;
import com.careerbuilder.search.relevancy.threadpool.ThreadPool;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Sort;
import org.apache.solr.common.util.SimpleOrderedMap;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;

public class NodeGenerator implements RecursionOp {

    public ResponseNode [] transform(NodeContext context, RequestNode [] requests, ResponseNode [] responses) throws IOException {
        ResponseNode [] resps = new ResponseNode[requests.length];
        FacetRunner [] runners = buildRunners(context, requests);
        List<Future<Waitable>> futures = ThreadPool.multiplex(runners);
        ThreadPool.demultiplex(futures);
        for(int i = 0; i < resps.length; ++i) {
            resps[i] = new ResponseNode(requests[i].type);
            mergeResponseValues(requests[i], resps[i], runners[i]);
        }
        return resps;
    }

    private void mergeResponseValues(RequestNode request, ResponseNode resp, FacetRunner runner) {
        int genLength = runner == null ? 0 : runner.buckets == null ? 0 :
                (int)runner.buckets.stream().filter(b -> runner.adapter.getStringValue(b) != null).count();
        int requestValsLength = request.values == null ? 0 : request.values.length;
        resp.values = new ResponseValue[requestValsLength + genLength];
        int k = addPassedInValues(request, resp);
        if(runner != null) {
            addGeneratedValues(resp, runner, k);
        }
    }

    private int addPassedInValues(RequestNode request, ResponseNode resp) {
        int k = 0;
        if(request.values != null) {
            for (; k < request.values.length; ++k) {
                resp.values[k] = new ResponseValue(request.values[k]);
                resp.values[k].normalizedValue = request.normalizedValues == null ? null : request.normalizedValues.get(k);
            }
        }
        return k;
    }

    private void addGeneratedValues(ResponseNode resp, FacetRunner runner, int k) {
        for (SimpleOrderedMap<Object> bucket: runner.buckets) {
            if(runner.adapter.getStringValue(bucket) != null)
            {
                ResponseValue respValue = new ResponseValue(runner.adapter.getStringValue(bucket));
                respValue.normalizedValue = runner.adapter.getMapValue(bucket);
                resp.values[k++] = respValue;
            }
        }
    }

    private FacetRunner [] buildRunners(NodeContext context,
                                        RequestNode [] requests) throws IOException {
        FacetRunner [] runners = new FacetRunner[requests.length];
        for(int i = 0; i < requests.length; ++i) {
            if(requests[i].discover_values) {
                // populate required docListAndSet once and only if necessary
                if(context.queryDomainList == null) {
                   context.queryDomainList =
                           context.req.getSearcher().getDocListAndSet(new MatchAllDocsQuery(),
                                   context.queryDomain, Sort.INDEXORDER, 0, 0);
                }
                FacetFieldAdapter adapter = new FacetFieldAdapter(context, requests[i].type);
                runners[i] = new FacetRunner(context,
                        adapter,
                        adapter.field,
                        0,
                        requests[i].limit);
            }
        }
        return runners;
    }
}
