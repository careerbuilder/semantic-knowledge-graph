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

import com.careerbuilder.search.relevancy.model.ResponseNode;
import com.careerbuilder.search.relevancy.NodeContext;
import com.careerbuilder.search.relevancy.runnable.QueryRunner;
import com.careerbuilder.search.relevancy.utility.ParseUtility;
import org.apache.lucene.search.Query;
import org.apache.solr.search.DocSet;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class QueryRunnerFactory
{
    private NodeContext context;
    private ResponseNode response;
    HashSet<Integer> fallbackIndices;

    public QueryRunnerFactory(NodeContext context, ResponseNode response, HashSet<Integer> fallbackIndices)
    {
        this.context = context;
        this.response = response;
        this.fallbackIndices = fallbackIndices;
    }

    protected List<QueryRunner> getQueryRunners(DocSet domain, String field, QueryRunner.QueryType type) {
        List<QueryRunner> runners = new LinkedList<>();
        if(fallbackIndices == null)
        {
            for (int k = 0; k < response.values.length; ++k)
            {
                runners.add(buildRunner(domain, type,
                        field, response.values[k].value.toLowerCase().trim(), k));
            }
        }
        else
        {
            for (Integer k : fallbackIndices)
            {
                runners.add(buildRunner(domain, type,
                        field, response.values[k].value.toLowerCase().trim(), k));
            }
        }
        return runners;
    }

    private QueryRunner buildRunner(DocSet domain, QueryRunner.QueryType type, String field, String value, int index)
    {
        Query query = ParseUtility.parseQueryString(
                field + ":\"" + value  + "\"",
                context.req);
        return new QueryRunner(context.req.getSearcher(), query, domain, type, index);
    }
}
