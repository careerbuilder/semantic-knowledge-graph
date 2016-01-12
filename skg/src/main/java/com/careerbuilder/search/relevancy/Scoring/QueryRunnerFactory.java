package com.careerbuilder.search.relevancy.Scoring;

import com.careerbuilder.search.relevancy.Models.ResponseNode;
import com.careerbuilder.search.relevancy.Models.ResponseValue;
import com.careerbuilder.search.relevancy.NodeContext;
import com.careerbuilder.search.relevancy.Runnable.QueryRunner;
import com.careerbuilder.search.relevancy.utility.ParseUtility;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.solr.search.DocSet;
import org.apache.solr.search.SolrIndexSearcher;

import java.util.HashSet;

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

    protected QueryRunner[] getQueryRunners(DocSet domain, String field) {
        QueryRunner [] runners = new QueryRunner[response.values.length];
        for(int k = 0; k < response.values.length; ++k) {
            if(fallbackIndices == null || fallbackIndices.contains(k)) {
                Query query = ParseUtility.parseQueryString(field
                                + ":\"" + response.values[k].value.toLowerCase().trim() + "\"",
                        context.req);
                runners[k] = new QueryRunner(context.req.getSearcher(), query, domain);
            }
        }
        return runners;
    }
}
