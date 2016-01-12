package com.careerbuilder.search.relevancy;

import com.careerbuilder.search.relevancy.Models.RelatednessRequest;
import com.careerbuilder.search.relevancy.Models.RequestNode;
import com.careerbuilder.search.relevancy.Models.ResponseNode;
import com.careerbuilder.search.relevancy.Models.ResponseValue;
import com.careerbuilder.search.relevancy.Normalization.NodeNormalizer;
import com.careerbuilder.search.relevancy.Scoring.NodeScorer;
import com.careerbuilder.search.relevancy.Generation.NodeGenerator;
import com.careerbuilder.search.relevancy.utility.ParseUtility;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.DocSet;
import org.apache.solr.search.SolrIndexSearcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(JMockit.class)
public class RequestTreeRecurserTest {

    @Mocked
    SolrQueryRequest solrRequest;
    @Mocked
    SolrIndexSearcher searcher;

    @Before
    public void init() throws IOException
    {
        new MockUp<NodeScorer>()
        {
            @Mock
            public ResponseNode [] transform(NodeContext context, RequestNode [] requests, ResponseNode [] responses)
            {
                responses = new ResponseNode[requests.length];
                for(int i = 0; i < requests.length; ++i) {
                    ResponseNode response = new ResponseNode(requests[i].type);
                    response.values = new ResponseValue[2];
                    response.values[0] = new ResponseValue("0");
                    response.values[1] = new ResponseValue("1");
                    responses[i] = response;
                }
                return responses;
            }
        };

        new MockUp<NodeGenerator>()
        {
            @Mock
            public ResponseNode [] transform(NodeContext context, RequestNode [] requests, ResponseNode [] responses)
            {
                return null;
            }
        };

        new MockUp<NodeNormalizer>()
        {
            @Mock
            public ResponseNode [] transform(NodeContext context, RequestNode [] requests, ResponseNode [] responses)
            {
                return null;
            }
        };



        new NonStrictExpectations() {{
            solrRequest.getSearcher(); returns(searcher);
        }};

        new NonStrictExpectations() {{
            try {
                searcher.getDocSet((Query) any, (DocSet) any);
                returns(null);
            } catch (Exception e) {}
        }};



        new NodeContext();
    }

    @Test
    public void recurseComparables_Null() throws IOException {

        RelatednessRequest request = new RelatednessRequest();
        NodeContext context = new NodeContext(request, solrRequest, null);
        RequestTreeRecurser target = new RequestTreeRecurser(context);
        ResponseNode[] actual = target.score();

        Assert.assertArrayEquals(null, actual);
    }


    @Test
    public void recurseComparables_One() throws IOException {

        RelatednessRequest request = new RelatednessRequest(
                new RequestNode[1]);
        request.compare[0] = new RequestNode(null, "testType");
        ResponseNode[] expected = new ResponseNode[1];
        expected[0] = new ResponseNode("testType");
        setTwoValues(expected[0]);
        NodeContext context = new NodeContext(request, solrRequest, null);
        RequestTreeRecurser target = new RequestTreeRecurser(context);

        ResponseNode[] actual = target.score();

        checkComparableTree(expected, actual);
    }

    @Test
    public void recurseComparables_TwoTrunkTree() throws IOException {
        new MockUp<ParseUtility>() {
            @Mock
            private Query parseQueryString(String qString, SolrQueryRequest req)
            {
                return new MatchAllDocsQuery();
            }
        };

        RelatednessRequest request = new RelatednessRequest(
                new RequestNode[2]);
        request.normalize=true;
        request.compare[0] = new RequestNode(null, "testType0");
        request.compare[1] = new RequestNode(null, "testType1");
        request.compare[0].compare = new RequestNode[2];
        request.compare[0].compare[0] = new RequestNode(null, "testType00");
        request.compare[0].compare[1] = new RequestNode(null, "testType01");
        ResponseNode[] expected = new ResponseNode[2];
        expected[0] = new ResponseNode("testType0");
        setTwoValues(expected[0]);
        setTwoCompares(expected[0].values[0]);
        setTwoCompares(expected[0].values[1]);
        setTwoValues(expected[0].values[0].compare[0]);
        setTwoValues(expected[0].values[0].compare[1]);
        setTwoValues(expected[0].values[1].compare[0]);
        setTwoValues(expected[0].values[1].compare[1]);
        expected[1] = new ResponseNode("testType1");
        setTwoValues(expected[1]);
        NodeContext context = new NodeContext(request, solrRequest, null);
        RequestTreeRecurser target = new RequestTreeRecurser(context);

        ResponseNode[] actual = target.score();

        checkComparableTree(expected, actual);
    }

    private void setTwoCompares(ResponseValue value) {
        value.compare = new ResponseNode[2];
        value.compare[0] = new ResponseNode("testType00");
        value.compare[1] = new ResponseNode("testType01");
    }

    private void setTwoValues(ResponseNode expected) {
        expected.values = new ResponseValue[2];
        expected.values[0] = new ResponseValue("0");
        expected.values[1] = new ResponseValue("1");
    }

    private void checkComparableTree(ResponseNode[] expected, ResponseNode[] actual)
    {
        if(actual!= null) {
            for(int i = 0; i < actual.length; ++i) {
                Assert.assertTrue(expected[i].type.compareTo(actual[i].type) == 0);
                for(int k = 0; k < actual[i].values.length; ++k) {
                    Assert.assertTrue(expected[i].values[k].value.compareTo(actual[i].values[k].value) == 0);
                    checkComparableTree(expected[i].values[k].compare, actual[i].values[k].compare);
                }
            }
        }
    }

}
