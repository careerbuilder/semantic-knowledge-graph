package com.careerbuilder.search.relevancy.Scoring;

import com.careerbuilder.search.relevancy.Models.RequestNode;
import com.careerbuilder.search.relevancy.Models.ResponseNode;
import com.careerbuilder.search.relevancy.NodeContext;
import com.careerbuilder.search.relevancy.RecursionOp;
import com.careerbuilder.search.relevancy.Runnable.QueryRunner;
import com.careerbuilder.search.relevancy.Runnable.Waitable;
import com.careerbuilder.search.relevancy.ThreadPool.ThreadPool;
import com.careerbuilder.search.relevancy.utility.ResponseUtility;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Future;

public class NodeScorer implements RecursionOp {

    public ResponseNode[] transform(NodeContext context, RequestNode[] requests, ResponseNode[] responses) {
        for(int i = 0; i < responses.length; ++i) {
            QueryRunnerFactory factory = new QueryRunnerFactory(context, responses[i], null);
            QueryRunner[] qRunners = context.request.return_popularity
                    ? factory.getQueryRunners(context.queryDomain, responses[i].type) : new QueryRunner[0];
            QueryRunner[] fgRunners = factory.getQueryRunners(context.fgDomain,responses[i].type);
            QueryRunner[] bgRunners = factory.getQueryRunners(context.bgDomain,responses[i].type);
            List<Future<Waitable>> q = ThreadPool.multiplex(qRunners);
            List<Future<Waitable>> fg = ThreadPool.multiplex(fgRunners);
            List<Future<Waitable>> bg = ThreadPool.multiplex(bgRunners);
            ThreadPool.demultiplex(q);
            ThreadPool.demultiplex(fg);
            ThreadPool.demultiplex(bg);
            String fallbackField = context.parameterSet.invariants.get(responses[i].type + ".fallback");
            if(fallbackField != null)
            {
                runFallback(context, responses[i], fgRunners, bgRunners, qRunners, fallbackField);
            }
            addQueryResults(responses[i], fgRunners, bgRunners, qRunners);
            processResponse(context, responses[i], requests[i]);
        }
        return responses;
    }

    private void runFallback(NodeContext context,
                             ResponseNode response,
                             QueryRunner[] fgRunners,
                             QueryRunner[] bgRunners,
                             QueryRunner[] qRunners,
                             String fallbackField) {
        HashSet<Integer> fallbackIndices = getFallbackIndices(fgRunners, context.request.min_popularity);
        QueryRunnerFactory factory = new QueryRunnerFactory(context, response, fallbackIndices);
        QueryRunner[] fallbackQRunners = context.request.return_popularity
                ? factory.getQueryRunners(context.queryDomain, fallbackField) : new QueryRunner[0];
        QueryRunner[] fallbackFGRunners = factory.getQueryRunners(context.fgDomain, fallbackField);
        QueryRunner[] fallbackBGRunners = factory.getQueryRunners(context.bgDomain, fallbackField);
        List<Future<Waitable>> q = ThreadPool.multiplex(fallbackQRunners);
        List<Future<Waitable>> fg = ThreadPool.multiplex(fallbackFGRunners);
        List<Future<Waitable>> bg = ThreadPool.multiplex(fallbackBGRunners);
        ThreadPool.demultiplex(q);
        ThreadPool.demultiplex(fg);
        ThreadPool.demultiplex(bg);
        replaceRunners(qRunners, fallbackQRunners, fallbackIndices);
        replaceRunners(fgRunners, fallbackFGRunners, fallbackIndices);
        replaceRunners(bgRunners, fallbackBGRunners, fallbackIndices);
    }

    private void replaceRunners(QueryRunner [] target, QueryRunner [] source, HashSet<Integer> targetPositions) {
        int k = 0;
        for(int i = 0; i < target.length; ++i) {
            if (targetPositions.contains(i)) {
               target[i] = source[k++];
            }
        }
    }

    private HashSet<Integer> getFallbackIndices(QueryRunner [] values, double minCount)
    {
        HashSet<Integer> indices = new HashSet<>();
        for(int i = 0; i < values.length; ++i) {
            if(values[i].result < minCount || values[i].result == 0) {
                indices.add(i);
            }
        }
        return indices;
    }

    private void addQueryResults(ResponseNode response, QueryRunner[] fgRunners, QueryRunner[] bgRunners, QueryRunner [] qRunners) {
        for(int k = 0; k < qRunners.length; ++k)
        {
            if (qRunners[k] != null)
            {
                response.values[k].popularity = qRunners[k].result;
            }
        }
        for(int k = 0; k < fgRunners.length; ++k)
        {
            if (fgRunners[k] != null)
            {
                response.values[k].foreground_popularity = fgRunners[k].result;
            }
        }
        for(int k = 0; k < bgRunners.length; ++k)
        {
            if(bgRunners[k] != null)
            {
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
            response.values = ResponseUtility.filterAndSortValues(response.values, request, context.request);
        }
    }

    private void relatednessScore(ResponseNode response, int fgTotal, int bgTotal) {
        for (int k = 0; k < response.values.length; ++k) {
            response.values[k].relatedness = RelatednessStrategy.z(fgTotal,
                    bgTotal, response.values[k].foreground_popularity, response.values[k].background_popularity);
        }
    }
}
