package com.careerbuilder.search.relevancy.runnable;

import com.careerbuilder.search.relevancy.generation.FacetFieldAdapter;
import com.careerbuilder.search.relevancy.model.RequestNode;
import com.careerbuilder.search.relevancy.model.ResponseValue;
import com.careerbuilder.search.relevancy.NodeContext;
import com.google.gson.Gson;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.response.SolrQueryResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.LinkedList;
import java.util.Map;

@RunWith(JMockit.class)
public class FacetRunnerTest {

    NodeContext context;
    RequestNode request;
    String field;
    Gson gson;

    @Before
    public void init()
    {
        context = new NodeContext();
        gson = new Gson();
        LinkedList<Query> queries = new LinkedList<>();
        queries.add(new TermQuery(new Term("testField1", "testQuery1")));
        queries.add(new TermQuery(new Term("testField2", "testQuery2")));
        Deencapsulation.setField(context, "fgQueries", queries);

        new MockUp<FacetFieldAdapter>(){
            @Mock public void $init(NodeContext context, String field) {}
        };

        request = new RequestNode();
        request.limit = 1;
        field = "testField1";
    }
    @Test
    public void parseResponse() {
        SolrQueryResponse resp = new SolrQueryResponse();
        SimpleOrderedMap<Object> root = new SimpleOrderedMap<>();
        SimpleOrderedMap<Object> queryFacet= new SimpleOrderedMap<>();
        SimpleOrderedMap<Object> fieldFacet= new SimpleOrderedMap<>();
        LinkedList<Object> buckets = new LinkedList<>();
        SimpleOrderedMap<Object> bucket1 = new SimpleOrderedMap<>();
        SimpleOrderedMap<Object> bucket2 = new SimpleOrderedMap<>();
        bucket1.add("val", "testValue1");
        bucket1.add("count", 1234);
        bucket2.add("val", "testValue2");
        bucket2.add("count", 4321);
        buckets.add(bucket1);
        buckets.add(bucket2);
        fieldFacet.add("buckets", buckets);
        queryFacet.add(FacetRunner.FIELD_FACET_NAME, fieldFacet);
        root.add(FacetRunner.QUERY_FACET_NAME, queryFacet);
        resp.add("facets", root);

        LinkedList<ResponseValue> expected = new LinkedList<>();
        expected.add(new ResponseValue("testValue1", 1234));
        expected.add(new ResponseValue("testValue2", 4321));


        FacetRunner target = new FacetRunner(context, "query", "testfield", 0);

        Deencapsulation.invoke(target, "parseResponse", resp);

        Assert.assertEquals(expected.size(), target.buckets.size());
        for(int i  = 0; i < target.buckets.size(); ++i)
        {
            Assert.assertEquals(buckets.get(i), target.buckets.get(i));
        }
    }

    @Test
    public void buildFacetParams(){
        FacetRunner target = new FacetRunner(context, "query", "testField", 0);

        MapSolrParams actual = Deencapsulation.invoke(target, "buildFacetParams");

        Assert.assertEquals("json".compareTo(actual.get("wt")), 0);
        Assert.assertEquals("1".compareTo(actual.get("facet.version")), 0);
        Assert.assertEquals("true".compareTo(actual.get(FacetParams.FACET)), 0);
    }

    @Test
    public void buildFacetJson(){
        FacetRunner target = new FacetRunner(context, "query", "testfield", 0);
        int limit = Math.max(request.limit, 25)*5;
        String expected = "{\"facet\":{\"queryFacet\":{\"query\":{\"facet\":{\"fieldFacet\":{\"field\":" +
                "{\"type\":\"field\",\"field\":\"testfield\",\"limit\":"+limit+"}}},\"q\":\"query\"}}}}";

        Map<String, Object> actual = Deencapsulation.invoke(target, "buildFacetJson");
        String actualStr = gson.toJson(actual);
        Assert.assertEquals(expected, actualStr);
    }

}
