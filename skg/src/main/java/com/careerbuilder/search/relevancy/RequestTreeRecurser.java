package com.careerbuilder.search.relevancy;

import com.careerbuilder.search.relevancy.Models.RelatednessRequest;
import com.careerbuilder.search.relevancy.Models.RequestNode;
import com.careerbuilder.search.relevancy.Models.ResponseNode;
import com.careerbuilder.search.relevancy.Models.ResponseValue;
import org.apache.solr.request.SolrQueryRequest;

import java.io.IOException;
import java.util.LinkedList;

public class RequestTreeRecurser {

    private RelatednessRequest request;
    private SolrQueryRequest solrReq;
    private NodeContext baseContext;
    private RecursionOp generator;
    private RecursionOp scorer;

    public RequestTreeRecurser(RelatednessRequest request, SolrQueryRequest solrReq) throws IOException {
        this(request, solrReq, new NodeGenerator(), new NodeScorer());
    }

    public RequestTreeRecurser(RelatednessRequest request,
                               SolrQueryRequest solrReq,
                               RecursionOp generator,
                               RecursionOp scorer) throws IOException {
        this.generator = generator;
        this.scorer = scorer;
        this.request = request;
        this.solrReq = solrReq;
        this.baseContext = new NodeContext(request, solrReq);
    }

    public ResponseNode[] score() throws IOException {
        ResponseNode[] responses = null;
        if(request.compare != null) {
            responses = generator.transform(baseContext, request.compare, null);
            responses = scorer.transform(baseContext, request.compare, responses);
            for(int i = 0; i < request.compare.length; ++i) {
                recurse(baseContext, responses[i], request.compare[i].children);
            }
        }
        return responses;
    }

    private void recurse(NodeContext context, ResponseNode parentResponse, RequestNode[] requests) throws IOException {
        if(requests != null) {
            for (ResponseValue value : parentResponse.values) {
                ResponseNode [] responses = generator.transform(context, requests, null);
                value.children = scorer.transform(context, requests, responses);
                for (int i = 0; i < requests.length; ++i) {
                    //TODO - filter context appropriately
                    recurse(context, value.children[i], requests[i].children);
                }
            }
        }
    }
}
