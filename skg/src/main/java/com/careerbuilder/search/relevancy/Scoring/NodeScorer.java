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

package com.careerbuilder.search.relevancy.scoring;

import com.careerbuilder.search.relevancy.model.RequestNode;
import com.careerbuilder.search.relevancy.model.ResponseNode;
import com.careerbuilder.search.relevancy.NodeContext;
import com.careerbuilder.search.relevancy.RecursionOp;
import com.careerbuilder.search.relevancy.model.ResponseValue;
import com.careerbuilder.search.relevancy.runnable.QueryRunner;
import com.careerbuilder.search.relevancy.runnable.Waitable;
import com.careerbuilder.search.relevancy.threadpool.ThreadPool;
import com.careerbuilder.search.relevancy.utility.ResponseUtility;
import org.apache.solr.common.SolrException;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;

public class NodeScorer implements RecursionOp {

    public ResponseNode[] transform(NodeContext context, RequestNode[] requests, ResponseNode[] responses) {
        for(int i = 0; i < responses.length; ++i) {
            QueryRunnerFactory factory = new QueryRunnerFactory(context, responses[i], null);

            List<QueryRunner> qRunners = new LinkedList<>();
            qRunners.addAll(factory.getQueryRunners(context.fgDomain,
                    responses[i].type, QueryRunner.QueryType.FG));
            qRunners.addAll(factory.getQueryRunners(context.bgDomain,
                    responses[i].type, QueryRunner.QueryType.BG));
            if(context.request.return_popularity)
                qRunners.addAll(factory.getQueryRunners(context.queryDomain, responses[i].type, QueryRunner.QueryType.Q));

            parallelQuery(qRunners);

            String fallbackField = context.parameterSet.invariants.get(responses[i].type + ".fallback");
            if(fallbackField != null)
            {
                new FallbackScorer().runFallback(context, responses[i], qRunners, fallbackField);
            }
            addQueryResults(responses[i], qRunners);
            processResponse(context, responses[i], requests[i]);
        }
        return responses;
    }

    protected static void parallelQuery(List<QueryRunner> qRunners)
    {
        List<Future<Waitable>> futures = new LinkedList<>();
        futures.addAll(ThreadPool.multiplex(qRunners.toArray(new Waitable[0])));
        ThreadPool.demultiplex(futures);
    }

    private void addQueryResults(ResponseNode response, List<QueryRunner> qRunners) {
        for(QueryRunner queryRunner : qRunners )
        {
            if (queryRunner != null)
            {
                setResultValue(response.values[queryRunner.index], queryRunner.result, queryRunner.type);
            }
        }
    }

    private void setResultValue(ResponseValue toSet, double result, QueryRunner.QueryType type)
    {
        switch(type)
        {
            case FG:
                toSet.foreground_popularity = result;
                break;
            case BG:
                toSet.background_popularity = result;
                break;
            case Q:
                toSet.popularity = result;
                break;
            default:
                throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Unknown QueryType.");
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
