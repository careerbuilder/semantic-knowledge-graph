package com.careerbuilder.search.relevancy.Scoring;

import com.careerbuilder.search.relevancy.Models.RequestNode;
import com.careerbuilder.search.relevancy.Models.ResponseNode;
import com.careerbuilder.search.relevancy.NodeContext;
import com.careerbuilder.search.relevancy.RecursionOp;
import com.careerbuilder.search.relevancy.Runnable.QueryRunner;
import com.careerbuilder.search.relevancy.ThreadPool.ThreadPool;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.PhraseQuery;
import org.apache.solr.search.DocSet;
import org.apache.solr.search.SolrIndexSearcher;

import java.io.IOException;
import java.util.Arrays;

public class NodeScorer implements RecursionOp {

    public ResponseNode[] transform(NodeContext context, RequestNode[] requests, ResponseNode[] responses) {
        for(int i = 0; i < responses.length; ++i) {
            QueryRunner[] qRunners = buildQueryRunners(context.req.getSearcher(), context.queryDomain, responses[i]);
            QueryRunner[] fgRunners = buildQueryRunners(context.req.getSearcher(), context.fgDomain, responses[i]);
            QueryRunner[] bgRunners = buildQueryRunners(context.req.getSearcher(), context.bgDomain, responses[i]);
            ThreadPool.multiplex(qRunners);
            ThreadPool.multiplex(fgRunners);
            ThreadPool.multiplex(bgRunners);
            ThreadPool.demultiplex(qRunners);
            ThreadPool.demultiplex(fgRunners);
            ThreadPool.demultiplex(bgRunners);
            buildResponse(responses[i], fgRunners, bgRunners, qRunners);
            processResponse(context, responses[i], requests[i]);
        }
        return responses;
    }

    private void buildResponse(ResponseNode response, QueryRunner[] fgRunners, QueryRunner[] bgRunners, QueryRunner [] qRunners) {
        for(int k = 0; k < fgRunners.length; ++k) {
            if(qRunners[k] != null) {
                response.values[k].popularity = qRunners[k].result;
            }
            if(fgRunners[k] != null) {
                response.values[k].foreground_popularity = fgRunners[k].result;
            }
            if(bgRunners[k] != null) {
                response.values[k].background_popularity= bgRunners[k].result;
            }
        }
    }

    private void processResponse(NodeContext context, ResponseNode response, RequestNode request)
    {
        if(response.values != null && response.values.length > 0) {
            int fgTotal = context.fgDomain.size();
            int bgTotal = context.bgDomain.size();
            relatednessScore(response, fgTotal, bgTotal);
            ScoreNormalizer.normalize(context, response.values);
            ResponseUtility.filterAndSortValues(response, request, context.request.min_count);
        }
    }

    private void relatednessScore(ResponseNode response, int fgTotal, int bgTotal) {
        for (int k = 0; k < response.values.length; ++k) {
            response.values[k].relatedness = RelatednessStrategy.z(fgTotal,
                    bgTotal, response.values[k].foreground_popularity, response.values[k].background_popularity);
        }
    }

    private QueryRunner[] buildQueryRunners(SolrIndexSearcher searcher,
                                          DocSet domain, ResponseNode response) {
        QueryRunner [] runners = new QueryRunner[response.values.length];
        for(int k = 0; k < response.values.length; ++k) {
            PhraseQuery query = new PhraseQuery();
            Arrays.asList(response.values[k].value.toLowerCase().split(" ")).forEach(t->query.add(new Term(response.type, t)));
            runners[k] = new QueryRunner(searcher, query, domain);
        }
        return runners;
    }
}
