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
