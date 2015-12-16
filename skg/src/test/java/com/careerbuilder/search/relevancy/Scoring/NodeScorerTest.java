package com.careerbuilder.search.relevancy.Scoring;

import com.careerbuilder.search.relevancy.Models.RelatednessRequest;
import com.careerbuilder.search.relevancy.Models.RequestNode;
import com.careerbuilder.search.relevancy.Models.ResponseNode;
import com.careerbuilder.search.relevancy.Models.ResponseValue;
import com.careerbuilder.search.relevancy.NodeContext;
import com.careerbuilder.search.relevancy.Runnable.QueryRunner;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.integration.junit4.JMockit;
import org.apache.solr.search.DocSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(JMockit.class)
public class NodeScorerTest {

    QueryRunner[] fgRunners = new QueryRunner[2];
    QueryRunner[] bgRunners = new QueryRunner[2];
    QueryRunner[] qRunners = new QueryRunner[2];
    ResponseNode response = new ResponseNode();
    ResponseNode pResponse = new ResponseNode();
    RequestNode request = new RequestNode();
    NodeContext context = new NodeContext(new RelatednessRequest());
    @Mocked DocSet fgDomain;
    @Mocked DocSet bgDomain;
    @Mocked ScoreNormalizer normalizer;

    @Before
    public void init() {
        response.type = "keywords.v1";
        response.values = new ResponseValue[0];
        fgRunners[0] = new QueryRunner(null, null, null);
        fgRunners[1] = new QueryRunner(null, null, null);
        bgRunners[0] = new QueryRunner(null, null, null);
        bgRunners[1] = new QueryRunner(null, null, null);
        qRunners[0] = new QueryRunner(null, null, null);
        qRunners[1] = new QueryRunner(null, null, null);
        fgRunners[0].result = 0;
        fgRunners[1].result = 1;
        bgRunners[0].result = 0;
        bgRunners[1].result = 1;
        qRunners[0].result = 0;
        qRunners[1].result = 1;

        request.limit = 2;

        context.bgDomain = bgDomain;
        context.fgDomain = fgDomain;

        new NonStrictExpectations(){{
            ScoreNormalizer.normalize((NodeContext) any, (ResponseValue[]) any);
            fgDomain.size(); result = 100;
            bgDomain.size(); result = 1000;
        }};

    }

    @Test
    public void buildResponse() throws IOException
    {
        response.values = new ResponseValue[2];
        response.values[0] = new ResponseValue("test");
        response.values[1] = new ResponseValue("test");
        NodeScorer target = new NodeScorer();
        Deencapsulation.invoke(target, "addQueryResults", response, fgRunners, bgRunners, qRunners);

        Assert.assertEquals(0, response.values[0].foreground_popularity, 1e-4);
        Assert.assertEquals(0, response.values[0].background_popularity, 1e-4);
        Assert.assertEquals(1, response.values[1].foreground_popularity, 1e-4);
        Assert.assertEquals(1, response.values[1].background_popularity, 1e-4);

    }

    @Test
    public void processResponse()
    {
        pResponse.values = new ResponseValue[2];
        pResponse.values[0] = new ResponseValue("test1");
        pResponse.values[0].popularity= 50;
        pResponse.values[0].foreground_popularity = 50;
        pResponse.values[0].background_popularity = 500;
        pResponse.values[1] = new ResponseValue("test2");
        pResponse.values[1].popularity= 50;
        pResponse.values[1].foreground_popularity = 50;
        pResponse.values[1].background_popularity = 500;
        NodeScorer target = new NodeScorer();
        Deencapsulation.invoke(target, "processResponse", context, pResponse, request);

        Assert.assertEquals(0, pResponse.values[0].relatedness, 1e-4);
        Assert.assertEquals(0, pResponse.values[1].relatedness, 1e-4);
    }


}
