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
import java.io.IOException;

public class NodeGenerator implements RecursionOp {

    public ResponseNode [] transform(NodeContext context, RequestNode [] requests, ResponseNode [] responses) throws IOException {
        ResponseNode [] resps = new ResponseNode[requests.length];
        FacetRunner [] runners = buildRunners(context, requests);
        ThreadPool.multiplex(runners);
        ThreadPool.demultiplex(runners);
        for(int i = 0; i < resps.length; ++i) {
            resps[i] = new ResponseNode(requests[i].type);
            mergeResponseValues(requests[i], resps[i], runners[i]);
        }
        return resps;
    }

    private void mergeResponseValues(RequestNode request, ResponseNode resp, FacetRunner runner) {
        int genLength = runner == null ? 0 : runner.responses.size();
        int requestValsLength = request.values == null ? 0 : request.values.length;
        resp.values = new ResponseValue[requestValsLength + genLength];
        int k = 0;
        for (; k < requestValsLength; ++k) {
                resp.values[k] = new ResponseValue(request.values[k], -1);
        }
        if(runner != null) {
            for (ResponseValue genValue : runner.responses) {
                resp.values[k++] = genValue;
            }
        }
    }

    public FacetRunner [] buildRunners(NodeContext context, RequestNode [] requests) throws IOException
    {
        FacetRunner [] runners = new FacetRunner[requests.length];
        for(int i = 0; i < requests.length; ++i) {
            if(requests[i].discoverValues) {
                String field = new FacetFieldAdapter(context).getFacetField(requests[i].type);
                // populate required docListAndSet once and only if necessary
                if(context.queryDomainList == null) {
                   context.queryDomainList =
                           context.req.getSearcher().getDocListAndSet(new MatchAllDocsQuery(),
                                   context.queryDomain, Sort.INDEXORDER, 0, 0);
                }
                runners[i] = new FacetRunner(context, requests[i], field);
            }
        }
        return runners;
    }
}
