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

import com.careerbuilder.search.relevancy.NodeContext;
import com.careerbuilder.search.relevancy.model.ResponseNode;
import com.careerbuilder.search.relevancy.runnable.QueryRunner;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class FallbackScorer
{
    public void runFallback(NodeContext context,
                             ResponseNode response,
                             List<QueryRunner> qRunners,
                             String fallbackField) {
        List<QueryRunner> fgQueryRunners = qRunners.stream().filter(q -> q.type == QueryRunner.QueryType.FG).collect(Collectors.toList());
        HashSet<Integer> fallbackIndices = getFallbackIndices(fgQueryRunners, context.request.min_popularity);
        QueryRunnerFactory factory = new QueryRunnerFactory(context, response, fallbackIndices);
        List<QueryRunner> fallbackRunners = new LinkedList<>();
        fallbackRunners.addAll(factory.getQueryRunners(context.fgDomain, fallbackField, QueryRunner.QueryType.FG));
        fallbackRunners.addAll(factory.getQueryRunners(context.bgDomain, fallbackField, QueryRunner.QueryType.BG));
        if(context.request.return_popularity)
        {
            fallbackRunners.addAll(factory.getQueryRunners(context.queryDomain, fallbackField, QueryRunner.QueryType.Q));
        }
        NodeScorer.parallelQuery(fallbackRunners);
        replaceRunners(qRunners, fallbackRunners);
    }

    protected HashSet<Integer> getFallbackIndices(List<QueryRunner> values, double minCount)
    {
        HashSet<Integer> indices = new HashSet<>();
        for(QueryRunner runner : values)
        {
            if(runner.result < minCount || runner.result == 0) {
                indices.add(runner.index);
            }
        }
        return indices;
    }

    protected void replaceRunners(List<QueryRunner> target, List<QueryRunner> replacers) {
        target.removeAll(replacers);
        target.addAll(replacers);
    }
}
