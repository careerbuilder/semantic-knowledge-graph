package com.careerbuilder.search.relevancy;

import com.careerbuilder.search.relevancy.Generation.NodeGenerator;
import com.careerbuilder.search.relevancy.Models.RelatednessRequest;
import com.careerbuilder.search.relevancy.Models.RequestNode;
import com.careerbuilder.search.relevancy.Models.ResponseNode;
import com.careerbuilder.search.relevancy.Models.ResponseValue;
import com.careerbuilder.search.relevancy.Scoring.NodeScorer;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;

import java.io.IOException;

public class RequestTreeRecurser {

    private RelatednessRequest request;
    private NodeContext baseContext;
    private RecursionOp generator;
    private RecursionOp scorer;

    public RequestTreeRecurser(NodeContext context) throws IOException {
        this(context, new NodeGenerator(), new NodeScorer());
    }

    public RequestTreeRecurser(NodeContext context,
                               RecursionOp generator,
                               RecursionOp scorer) throws IOException {
        this.generator = generator;
        this.scorer = scorer;
        this.request = context.request;
        this.baseContext = context;
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

    private void recurse(NodeContext parentContext, ResponseNode parentResponse, RequestNode[] requests) throws IOException {
        if(requests != null) {
            for (ResponseValue value : parentResponse.values) {
                TermQuery filter = new TermQuery(new Term(parentResponse.type, value.value));
                NodeContext context = new NodeContext(parentContext, filter);
                ResponseNode [] responses = generator.transform(context, requests, null);
                value.children = scorer.transform(context, requests, responses);
                for (int i = 0; i < requests.length; ++i) {
                    recurse(context, value.children[i], requests[i].children);
                }
            }
        }
    }
}
