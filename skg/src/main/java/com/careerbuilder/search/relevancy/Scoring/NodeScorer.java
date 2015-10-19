package com.careerbuilder.search.relevancy.Scoring;

import com.careerbuilder.search.relevancy.Models.RequestNode;
import com.careerbuilder.search.relevancy.Models.ResponseNode;
import com.careerbuilder.search.relevancy.Models.ResponseValue;
import com.careerbuilder.search.relevancy.NodeContext;
import com.careerbuilder.search.relevancy.RecursionOp;
import com.careerbuilder.search.relevancy.Runnable.QueryRunner;
import com.careerbuilder.search.relevancy.ThreadPool.ThreadPool;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.solr.search.DocSet;
import org.apache.solr.search.SolrIndexSearcher;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class NodeScorer implements RecursionOp {

    public ResponseNode[] transform(NodeContext context, RequestNode[] requests, ResponseNode[] responses) {
        for(int i = 0; i < responses.length; ++i) {
            QueryRunner[] qRunners =
                    buildQueryRunners(context.req.getSearcher(), context.queryDomain, responses[i].values, responses[i].type, null);
            QueryRunner[] fgRunners =
                    buildQueryRunners(context.req.getSearcher(), context.fgDomain, responses[i].values, responses[i].type, null);
            QueryRunner[] bgRunners =
                    buildQueryRunners(context.req.getSearcher(), context.bgDomain, responses[i].values, responses[i].type, null);
            ThreadPool.multiplex(qRunners);
            ThreadPool.multiplex(fgRunners);
            ThreadPool.multiplex(bgRunners);
            ThreadPool.demultiplex(qRunners);
            ThreadPool.demultiplex(fgRunners);
            ThreadPool.demultiplex(bgRunners);
            String fallbackField = context.parameterSet.invariants.get(responses[i].type + ".fallback");
            if(fallbackField != null)
            {
                runFallback(context, responses[i], fgRunners, fallbackField);
            }
            addQueryResults(responses[i], fgRunners, bgRunners, qRunners);
            processResponse(context, responses[i], requests[i]);
        }
        return responses;
    }

    private void runFallback(NodeContext context, ResponseNode response, QueryRunner[] fgRunners, String fallbackField) {
        HashSet<Integer> fallbackIndices = getFallbackIndices(fgRunners, context.request.min_count);
        QueryRunner [] fallbackRunners = buildQueryRunners(context.req.getSearcher(),
                context.fgDomain, response.values, fallbackField, fallbackIndices);
        ThreadPool.multiplex(fallbackRunners);
        ThreadPool.demultiplex(fallbackRunners);
        replaceRunners(fgRunners, fallbackRunners, fallbackIndices);
    }

    private void replaceRunners(QueryRunner [] target, QueryRunner [] source, HashSet<Integer> targetPositions) {
        int k = 0;
        for(int i = 0; i < target.length; ++i) {
            if (targetPositions.contains(i)) {
               target[i] = source[k++];
            }
        }
    }

    private HashSet<Integer> getFallbackIndices(QueryRunner [] values, int minCount)
    {
        HashSet<Integer> indices = new HashSet<>();
        for(int i = 0; i < values.length; ++i) {
            if(values[i].result <= minCount) {
                indices.add(i);
            }
        }
        return indices;
    }

    private void addQueryResults(ResponseNode response, QueryRunner[] fgRunners, QueryRunner[] bgRunners, QueryRunner [] qRunners) {
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
                                            DocSet domain,
                                            ResponseValue[] values,
                                            String responseType,
                                            HashSet<Integer> indices) {
        QueryRunner [] runners = new QueryRunner[values.length];
        for(int k = 0; k < values.length; ++k) {
            if(indices == null || indices.contains(k)) {
                Query query = new TermQuery(new Term(responseType, values[k].value.toLowerCase().trim()));
                runners[k] = new QueryRunner(searcher, query, domain);
            }
        }
        return runners;
    }
}
