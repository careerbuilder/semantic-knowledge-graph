package com.careerbuilder.search.relevancy.scoring;

import com.careerbuilder.search.relevancy.model.RelatednessRequest;
import com.careerbuilder.search.relevancy.model.ResponseNode;
import com.careerbuilder.search.relevancy.model.ResponseValue;
import com.careerbuilder.search.relevancy.NodeContext;
import com.careerbuilder.search.relevancy.runnable.QueryRunner;
import com.careerbuilder.search.relevancy.utility.ParseUtility;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.DocSet;
import org.apache.solr.search.SolrIndexSearcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

@RunWith(JMockit.class)
public class QueryRunnerFactoryTest
{

    @Mocked
    DocSet domain;
    NodeContext context = new NodeContext(new RelatednessRequest());
    ResponseNode node;
    @Mocked
    SolrIndexSearcher unusedSearcher;
    @Mocked
    SolrQueryRequest unused;
    @Mocked
    SolrParams defTypeMap;

    @Before
    public void init()
    {
        unused.setParams(defTypeMap);
        context.req = unused;

        new MockUp<ParseUtility>()
        {
            @Mock
            public Query parseQueryString(String query, SolrQueryRequest req)
            {
                return new TermQuery(new Term("test",query));
            }
        };

        new ParseUtility();
        new NonStrictExpectations() {{
            unused.getSearcher(); returns(unusedSearcher);
        }};

        ResponseValue[] values = new ResponseValue[4];
        values[0] = new ResponseValue("v0");
        values[1] = new ResponseValue("v1");
        values[2] = new ResponseValue("v2");
        values[3] = new ResponseValue("v3");

        node = new ResponseNode("testType");
        node.values = values;
    }

    @Test
    public void getQueryRunners() throws IOException
    {
        QueryRunnerFactory target = new QueryRunnerFactory(context, node, null);
        List<QueryRunner> actual = target.getQueryRunners(domain, "testField", QueryRunner.QueryType.FG);

        Assert.assertEquals(4, actual.size());

        Assert.assertEquals("test:testField:\"v0\"",((Query)Deencapsulation.getField(actual.get(0), "query")).toString());
        Assert.assertEquals("test:testField:\"v1\"",((Query)Deencapsulation.getField(actual.get(1), "query")).toString());
        Assert.assertEquals("test:testField:\"v2\"",((Query)Deencapsulation.getField(actual.get(2), "query")).toString());
        Assert.assertEquals("test:testField:\"v3\"",((Query)Deencapsulation.getField(actual.get(3), "query")).toString());

        Assert.assertEquals(QueryRunner.QueryType.FG, actual.get(0).type);
        Assert.assertEquals(QueryRunner.QueryType.FG, actual.get(1).type);
        Assert.assertEquals(QueryRunner.QueryType.FG, actual.get(2).type);
        Assert.assertEquals(QueryRunner.QueryType.FG, actual.get(3).type);

        Assert.assertEquals(0, actual.get(0).index);
        Assert.assertEquals(1, actual.get(1).index);
        Assert.assertEquals(2, actual.get(2).index);
        Assert.assertEquals(3, actual.get(3).index);
    }

    @Test
    public void getQueryRunnersFallback() throws IOException
    {
        HashSet<Integer> fallback = new HashSet<>();
        fallback.add(1);
        QueryRunnerFactory target = new QueryRunnerFactory(context, node, fallback);
        List<QueryRunner> actual = target.getQueryRunners(domain, "testField", QueryRunner.QueryType.FG);

        Assert.assertEquals(1, actual.size());
        Assert.assertEquals("test:testField:\"v1\"",((Query)Deencapsulation.getField(actual.get(0), "query")).toString());
        Assert.assertEquals(QueryRunner.QueryType.FG, actual.get(0).type);
        Assert.assertEquals(1, actual.get(0).index);
    }
}
