package com.careerbuilder.search.relevancy.Scoring;

import com.careerbuilder.search.relevancy.Models.RequestNode;
import com.careerbuilder.search.relevancy.Models.ResponseNode;
import com.careerbuilder.search.relevancy.Models.ResponseValue;
import com.careerbuilder.search.relevancy.NodeContext;
import com.careerbuilder.search.relevancy.RecursionOp;
import com.careerbuilder.search.relevancy.Runnable.QueryRunner;
import com.careerbuilder.search.relevancy.ThreadPool.ThreadPool;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.solr.search.DocSet;
import org.apache.solr.search.SolrIndexSearcher;

import java.util.Arrays;
import java.util.HashSet;

public class NodeScorer implements RecursionOp {

    public ResponseNode[] transform(NodeContext context, RequestNode[] requests, ResponseNode[] responses) {
        for(int i = 0; i < responses.length; ++i) {
            QueryRunner[] fgRunners = buildQueryRunners(context.req.getSearcher(), context.fgDomain, responses[i]);
            QueryRunner[] bgRunners = buildQueryRunners(context.req.getSearcher(), context.bgDomain, responses[i]);
            ThreadPool.multiplex(fgRunners);
            ThreadPool.multiplex(bgRunners);
            ThreadPool.demultiplex(fgRunners);
            ThreadPool.demultiplex(bgRunners);
            buildResponse(context, responses[i], fgRunners, bgRunners);
            processResponse(context, responses[i], requests[i]);
        }
        return responses;
    }

    private void buildResponse(NodeContext context, ResponseNode response, QueryRunner[] fgRunners, QueryRunner[] bgRunners) {
        for(int k = 0; k < fgRunners.length; ++k) {
            if(fgRunners[k] != null) {
                response.values[k].magnitude = fgRunners[k].result;
            }
            if(bgRunners[k] != null) {
                response.values[k].popularity = bgRunners[k].result;
            }
        }
    }

    private void processResponse(NodeContext context, ResponseNode response, RequestNode request)
    {
        int fgTotal = context.fgDomain.size();
        int bgTotal = context.bgDomain.size();
        for(int k = 0; k < response.values.length; ++k) {
            response.values[k].relatedness = relatednessScore(fgTotal,
                    bgTotal, response.values[k].magnitude, response.values[k].popularity);
        }
        filterAndSortValues(response, request);
    }

    // keeps all passed in values, up to request.limit
    // keeps as many generated values as possible up to request.limit
    public void filterAndSortValues(ResponseNode response, RequestNode request) {
        response.sortResponseValues(request.sort);
        int limit = Math.min(response.values.length, request.limit);
        ResponseValue[] shrunk = Arrays.copyOf(response.values, limit);
        if(request.values != null && request.values.length > 0) {
            HashSet<String> keepSet = new HashSet<>();
            keepSet.addAll(Arrays.asList(request.values));
            ResponseValue[] keepArray = new ResponseValue[request.values.length];
            int k = 0;
            for (int i = limit; i < response.values.length; ++i) {
                if (keepSet.contains(response.values[i].value)) {
                    keepArray[k++] = new ResponseValue(response.values[i]);
                }
            }
            System.arraycopy(keepArray, 0, shrunk, Math.max(limit-k,0), Math.min(k, limit));
        }
        response.values = shrunk;
    }

    private double relatednessScore(int fgTotal, int bgTotal, double fgCount, double bgCount) {
        return new RelatednessStrategy(fgTotal, bgTotal, fgCount, bgCount).z();
    }

    private QueryRunner[] buildQueryRunners(SolrIndexSearcher searcher,
                                          DocSet domain, ResponseNode response) {
        QueryRunner [] runners = new QueryRunner[response.values.length];
        for(int k = 0; k < response.values.length; ++k) {
            Query query = new TermQuery(new Term(response.type, response.values[k].value.toLowerCase()));
            runners[k] = new QueryRunner(searcher, query, domain);
        }
        return runners;
    }
}
