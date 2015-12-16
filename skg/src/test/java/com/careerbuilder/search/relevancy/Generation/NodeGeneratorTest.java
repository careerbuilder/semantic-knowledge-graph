package com.careerbuilder.search.relevancy.Generation;

import com.careerbuilder.search.relevancy.utility.MapUtility;
import com.careerbuilder.search.relevancy.Models.RelatednessRequest;
import com.careerbuilder.search.relevancy.Models.RequestNode;
import com.careerbuilder.search.relevancy.Models.ResponseNode;
import com.careerbuilder.search.relevancy.Models.ResponseValue;
import com.careerbuilder.search.relevancy.NodeContext;
import com.careerbuilder.search.relevancy.Runnable.FacetRunner;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.DocListAndSet;
import org.apache.solr.search.DocSet;
import org.apache.solr.search.SolrIndexSearcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.*;

@RunWith(JMockit.class)
public class NodeGeneratorTest {

    ResponseNode response;
    RequestNode request;
    @Mocked FacetRunner runner;
    @Cascading final NodeContext context = new NodeContext(new RelatednessRequest());
    @Mocked SolrQueryRequest unused;
    @Mocked SolrIndexSearcher unusedSearcher;
    FacetFieldAdapter adapter;

    @Before
    public void init() {
        runner.buckets= new LinkedList<>();

        request = new RequestNode(null, "testType");
        request.values = new String[] {"passedInValue1", "passedInValue2"};

        response = new ResponseNode("testType");
        context.req = unused;

        new MockUp<SolrIndexSearcher>()
        {
            @Mock
            public DocListAndSet getDocListAndSet(Query q, DocSet d, Sort s, int one, int two)
            {
                return null;
            }
        };

        new MockUp<FacetFieldAdapter> () {
            @Mock public void $init(NodeContext context, String field) { }
            @Mock public String getStringValue(SimpleOrderedMap<Object> bucket) {
                return (String)bucket.get("val");
            }
            @Mock public SimpleOrderedMap<String> getMapValue(SimpleOrderedMap<Object> bucket) {
                SimpleOrderedMap<String> map = new SimpleOrderedMap<>();
                map.add("name", (String)bucket.get("val"));
                return map;
            }
        };

        new NonStrictExpectations() {{
            context.req.getSearcher(); returns(unusedSearcher);
        }};

        adapter = new FacetFieldAdapter(null, "testField");
    }

    @Test
    public void buildRunners_noDiscover()
    {
        RequestNode [] requests = new RequestNode[1];
        FacetFieldAdapter [] adapters = new FacetFieldAdapter[1];
        requests[0] = new RequestNode(null, "testField");

        NodeGenerator target = new NodeGenerator();
        FacetRunner [] runners = Deencapsulation.invoke(target, "buildRunners", context, requests, adapters);

        Assert.assertEquals(null, runners[0]);
    }

    @Test
    public void buildRunners_discover()
    {
        RequestNode [] requests = new RequestNode[1];
        FacetFieldAdapter [] adapters = new FacetFieldAdapter[1];
        requests[0] = new RequestNode(null, "testField");
        requests[0].limit = 10;
        requests[0].discover_values = true;

        new Expectations() {{
            new FacetRunner(context, null, 10);
        }};

        NodeGenerator target = new NodeGenerator();
        Deencapsulation.invoke(target, "buildRunners", context, requests, adapters);
        Assert.assertNotEquals(null, adapters[0]);
    }


    @Test
    public void addPassedInValues_noNormalized() throws IOException
    {
        ResponseNode response = new ResponseNode("testField");
        response.values = new ResponseValue[2];
        ResponseValue [] expecteds = new ResponseValue[2];
        expecteds[0] = new ResponseValue("passedInValue1");
        expecteds[1] = new ResponseValue("passedInValue2");

        NodeGenerator target = new NodeGenerator();
        Deencapsulation.invoke(target, "addPassedInValues", request, response);

        Assert.assertEquals(2, response.values.length);
        for(ResponseValue expected : expecteds) {
            Assert.assertTrue(Arrays.asList(response.values).contains(expected));
        }

    }

    @Test
    public void addPassedInValues_normalized() throws IOException
    {
        ResponseNode response = new ResponseNode("testField");
        response.values = new ResponseValue[2];
        ResponseValue [] expecteds = new ResponseValue[2];
        expecteds[0] = new ResponseValue("passedInValue1");
        expecteds[0].normalizedValue = new SimpleOrderedMap<>();
        expecteds[0].normalizedValue.add("name", "passedInValue1");
        expecteds[1] = new ResponseValue("passedInValue2");
        expecteds[1].normalizedValue = new SimpleOrderedMap<>();
        expecteds[1].normalizedValue.add("name", "passedInValue2");
        request.normalizedValues = new LinkedList<>();
        request.normalizedValues.add(expecteds[0].normalizedValue);
        request.normalizedValues.add(expecteds[1].normalizedValue);

        NodeGenerator target = new NodeGenerator();
        Deencapsulation.invoke(target, "addPassedInValues", request, response);

        Assert.assertEquals(2, response.values.length);
        for(int i = 0; i < expecteds.length; ++i) {
            Assert.assertTrue(response.values[i].equals(expecteds[i]));
            Assert.assertTrue(MapUtility.mapsEqual(response.values[i].normalizedValue, expecteds[i].normalizedValue));
        }

    }

    @Test
    public void merge() throws IOException
    {
        RequestNode request = new RequestNode(null, "type");
        NodeGenerator target = new NodeGenerator();
        new Expectations(target){{
            Deencapsulation.invoke(target, "addPassedInValues", request, response); returns(1);
            Deencapsulation.invoke(target, "addGeneratedValues", response, runner, adapter, 1);
        }};

        Deencapsulation.invoke(target, "mergeResponseValues", request, response, runner, adapter);
    }

    @Test
    public void addGeneratedValues() throws IOException
    {
        ResponseNode response = new ResponseNode("testField");
        response.values = new ResponseValue[2];
        FacetRunner runner = new FacetRunner(null, null, "testField", 0);
        FacetFieldAdapter adapter = new FacetFieldAdapter(null, "testField");
        runner.buckets = new LinkedList<>();
        SimpleOrderedMap<Object> generatedValue1 = new SimpleOrderedMap<>();
        generatedValue1.add("val", "generatedValue1");
        SimpleOrderedMap<Object> generatedValue2 = new SimpleOrderedMap<>();
        generatedValue2.add("val", "generatedValue2");
        runner.buckets.add(generatedValue1);
        runner.buckets.add(generatedValue2);
        ResponseValue [] expecteds = new ResponseValue[2];
        expecteds[0] = new ResponseValue("generatedValue1");
        expecteds[0].normalizedValue = new SimpleOrderedMap<>();
        expecteds[0].normalizedValue.add("name", "generatedValue1");
        expecteds[1] = new ResponseValue("generatedValue2");
        expecteds[1].normalizedValue = new SimpleOrderedMap<>();
        expecteds[1].normalizedValue.add("name", "generatedValue2");

        NodeGenerator target = new NodeGenerator();
        Deencapsulation.invoke(target, "addGeneratedValues", response, runner, adapter, 0);

        Assert.assertEquals(2, response.values.length);
        for(int i = 0; i < expecteds.length; ++i) {
            Assert.assertTrue(response.values[i].equals(expecteds[i]));
            Assert.assertTrue(MapUtility.mapsEqual(response.values[i].normalizedValue, expecteds[i].normalizedValue));
        }
    }

}
