package com.careerbuilder.search.relevancy.Scoring;

import com.careerbuilder.search.relevancy.Models.RelatednessRequest;
import com.careerbuilder.search.relevancy.Models.ResponseNode;
import com.careerbuilder.search.relevancy.Models.ResponseValue;
import com.careerbuilder.search.relevancy.NodeContext;
import com.careerbuilder.search.relevancy.Runnable.QueryRunner;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.DocListAndSet;
import org.apache.solr.search.DocSet;
import org.apache.solr.search.SolrIndexSearcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.HashSet;

@RunWith(JMockit.class)
public class QueryRunnerFactoryTest
{

    @Mocked
    DocSet domain;
    @Cascading final NodeContext context = new NodeContext(new RelatednessRequest());
    ResponseNode node;
    @Mocked
    SolrIndexSearcher unusedSearcher;
    @Mocked
    SolrQueryRequest unused;

    @Before
    public void init()
    {
        context.req = unused;

        new NonStrictExpectations() {{
            context.req.getSearcher(); returns(unusedSearcher);
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
        QueryRunner[] actual = target.getQueryRunners(domain, "testField");

        Assert.assertEquals("testField:v0",((Query)Deencapsulation.getField(actual[0], "query")).toString());
        Assert.assertEquals("testField:v1",((Query)Deencapsulation.getField(actual[1], "query")).toString());
        Assert.assertEquals("testField:v2",((Query)Deencapsulation.getField(actual[2], "query")).toString());
        Assert.assertEquals("testField:v3",((Query)Deencapsulation.getField(actual[3], "query")).toString());
    }

    @Test
    public void getQueryRunnersFallback() throws IOException
    {
        HashSet<Integer> fallback = new HashSet<>();
        fallback.add(1);
        QueryRunnerFactory target = new QueryRunnerFactory(context, node, fallback);
        QueryRunner[] actual = target.getQueryRunners(domain, "testField");

        Assert.assertEquals(null, actual[0]);
        Assert.assertEquals("testField:v1",((Query)Deencapsulation.getField(actual[1], "query")).toString());
        Assert.assertEquals(null, actual[2]);
        Assert.assertEquals(null, actual[3]);
    }
}
