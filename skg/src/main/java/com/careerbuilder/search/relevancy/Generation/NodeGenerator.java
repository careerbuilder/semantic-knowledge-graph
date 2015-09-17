package com.careerbuilder.search.relevancy.Generation;

import com.careerbuilder.search.relevancy.Models.RequestNode;
import com.careerbuilder.search.relevancy.Models.ResponseNode;
import com.careerbuilder.search.relevancy.Models.ResponseValue;
import com.careerbuilder.search.relevancy.NodeContext;
import com.careerbuilder.search.relevancy.RecursionOp;
import com.careerbuilder.search.relevancy.Runnable.FacetRunner;
import com.careerbuilder.search.relevancy.ThreadPool.ThreadPool;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Sort;
import org.apache.solr.common.util.SimpleOrderedMap;

import java.io.IOException;

public class NodeGenerator implements RecursionOp {

    public ResponseNode [] transform(NodeContext context, RequestNode [] requests, ResponseNode [] responses) throws IOException {
        ResponseNode [] resps = new ResponseNode[requests.length];
        FacetFieldAdapter [] adapters = new FacetFieldAdapter[requests.length];
        FacetRunner [] runners = buildRunners(context, requests, adapters);
        ThreadPool.multiplex(runners);
        ThreadPool.demultiplex(runners);
        for(int i = 0; i < resps.length; ++i) {
            resps[i] = new ResponseNode(requests[i].type);
            mergeResponseValues(requests[i], resps[i], runners[i], adapters[i]);
        }
        return resps;
    }

    private void mergeResponseValues(RequestNode request, ResponseNode resp, FacetRunner runner, FacetFieldAdapter adapter) {
        int genLength = runner == null ? 0 : runner.buckets.size();
        int requestValsLength = request.values == null ? 0 : request.values.length;
        resp.values = new ResponseValue[requestValsLength + genLength];
        int k = addPassedInValues(request, resp);
        if(runner != null) {
            addGeneratedValues(resp, runner, adapter, k);
        }
    }

    private int addPassedInValues(RequestNode request, ResponseNode resp) {
        int k = 0;
        if(request.values != null) {
            for (; k < resp.values.length; ++k) {
                resp.values[k] = new ResponseValue(request.values[k]);
                resp.values[k].normalizedValue = request.normalizedValues == null ? null : request.normalizedValues.get(k);
            }
        }
        return k;
    }

    private void addGeneratedValues(ResponseNode resp, FacetRunner runner, FacetFieldAdapter adapter, int k) {
        for (SimpleOrderedMap<Object> bucket: runner.buckets) {
            ResponseValue respValue = new ResponseValue(adapter.getStringValue(bucket));
            respValue.normalizedValue = adapter.getMapValue(bucket);
            resp.values[k++] = respValue;
        }
    }

    private FacetRunner [] buildRunners(NodeContext context, RequestNode [] requests, FacetFieldAdapter [] adapters) throws IOException {
        FacetRunner [] runners = new FacetRunner[requests.length];
        for(int i = 0; i < requests.length; ++i) {
            adapters[i] = new FacetFieldAdapter(context, requests[i].type);
            if(requests[i].discover_values) {
                // populate required docListAndSet once and only if necessary
                if(context.queryDomainList == null) {
                   context.queryDomainList =
                           context.req.getSearcher().getDocListAndSet(new MatchAllDocsQuery(),
                                   context.queryDomain, Sort.INDEXORDER, 0, 0);
                }
                runners[i] = new FacetRunner(context, adapters[i].field, requests[i].limit);
            }
        }
        return runners;
    }
}
