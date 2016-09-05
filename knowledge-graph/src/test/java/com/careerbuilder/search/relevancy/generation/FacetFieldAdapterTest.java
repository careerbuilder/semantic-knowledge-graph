package com.careerbuilder.search.relevancy.generation;

import com.careerbuilder.search.relevancy.FieldChecker;
import com.careerbuilder.search.relevancy.model.ParameterSet;
import com.careerbuilder.search.relevancy.NodeContext;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.request.SolrQueryRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@RunWith(JMockit.class)
public class FacetFieldAdapterTest {

    private MapSolrParams invariants;
    private List<String> fieldList;
    private String[] fieldNames = new String[]
            {
                    "title",
                    "title.top",
                    "title.id-title",
                    "title.top.id-title",
                    "tags",
                    "tags.top",
                    "tags.id-title",
                    "tags.top.id-title",
                    "rank",
                    "rank.level-description",
                    "field.with.dots",
                    "field.with.dots.id-title",
                    "field.with.dots.id"
            };

    @Before
    public void init() {
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("title.facet-field", "id-title");
        paramMap.put("title.key", "id");
        paramMap.put("title.top.facet-field", "id-title");
        paramMap.put("tags.facet-field", "id-title");
        paramMap.put("tags.key", "id");
        paramMap.put("tags.top.facet-field", "id-title");
        paramMap.put("tags.top.key", "id");
        paramMap.put("rank.facet-field", "level-description");
        paramMap.put("rank.key", "level");
        paramMap.put("field.with.dots.facet-field", "id-value");
        paramMap.put("field.with.dots.key", "id");

        new MockUp<FieldChecker>()
        {
            @Mock void checkField(SolrQueryRequest req, String field, String facetField)
            {
                if(!fieldList.contains(field))
                    throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "you tell me do things i come runnin");
            }
        };

        fieldList = Arrays.asList(fieldNames);
        invariants = new MapSolrParams(paramMap);
    }


    @Test(expected = SolrException.class)
    public void testAdaptNonExistantField() throws IOException
    {
        NodeContext context = new NodeContext(new ParameterSet(null, null, invariants));
        FacetFieldAdapter target = new FacetFieldAdapter(context, "bogus");
    }


    @Test
    public void testAdaptTitle() throws IOException
    {
        NodeContext context = new NodeContext(new ParameterSet(null, null, invariants));
        FacetFieldAdapter target = new FacetFieldAdapter(context, "title");

        String actual = target.field;
        Assert.assertEquals("title.id-title", actual);
    }

    @Test
    public void testAdaptTitleTop()
    {
        NodeContext context = new NodeContext(new ParameterSet(null, null, invariants));
        FacetFieldAdapter target = new FacetFieldAdapter(context, "title.top");

        String actual = target.field;
        Assert.assertEquals("title.top.id-title", actual);
    }

    @Test
    public void testAdaptRank() throws IOException
    {
        NodeContext context = new NodeContext(new ParameterSet(null,null,invariants));
        FacetFieldAdapter target = new FacetFieldAdapter(context, "rank");

        String actual = target.field;
        Assert.assertEquals("rank.level-description", actual);
    }


    @Test
    public void getMapValue_Rank() throws IOException
    {
        NodeContext context = new NodeContext(new ParameterSet(null, null, invariants));
        FacetFieldAdapter target = new FacetFieldAdapter(context, "rank");

        SimpleOrderedMap<Object> resultBucket = new SimpleOrderedMap<>();
        resultBucket.add("val", "99^testdescription");
        SimpleOrderedMap<String> actual = target.getMapValue(resultBucket);

        Assert.assertEquals("99", actual.get("level"));
        Assert.assertEquals("testdescription", actual.get("description"));
    }

    @Test
    public void getStringValue_Rank() throws IOException
    {
        NodeContext context = new NodeContext(new ParameterSet(null,null,invariants));
        FacetFieldAdapter target = new FacetFieldAdapter(context, "rank");

        SimpleOrderedMap<Object> resultBucket = new SimpleOrderedMap<>();
        resultBucket.add("val", "99^testdescription");
        String actual = target.getStringValue(resultBucket);

        Assert.assertEquals("99", actual);
    }

    @Test
    public void getMapValue_Title() throws IOException
    {
        NodeContext context = new NodeContext(new ParameterSet(null,null,invariants));
        FacetFieldAdapter target = new FacetFieldAdapter(context, "title");

        SimpleOrderedMap<Object> resultBucket = new SimpleOrderedMap<>();
        resultBucket.add("val", "99^testtitle");
        SimpleOrderedMap<String> actual = target.getMapValue(resultBucket);

        Assert.assertEquals("99", actual.get("id"));
        Assert.assertEquals("testtitle", actual.get("title"));
    }

    @Test
    public void getStringValue_Title() throws IOException
    {
        NodeContext context = new NodeContext(new ParameterSet(null,null,invariants));
        FacetFieldAdapter target = new FacetFieldAdapter(context, "title");

        SimpleOrderedMap<Object> resultBucket = new SimpleOrderedMap<>();
        resultBucket.add("val", "99^testtitle");
        String actual = target.getStringValue(resultBucket);

        Assert.assertEquals("99", actual);
    }

    @Test
    public void getMapValue_Tags() throws IOException
    {
        NodeContext context = new NodeContext(new ParameterSet(null,null,invariants));
        FacetFieldAdapter target = new FacetFieldAdapter(context, "tags");

        SimpleOrderedMap<Object> resultBucket = new SimpleOrderedMap<>();
        resultBucket.add("val", "99^testtitle");
        SimpleOrderedMap<String> actual = target.getMapValue(resultBucket);

        Assert.assertEquals("99", actual.get("id"));
        Assert.assertEquals("testtitle", actual.get("title"));
    }


    @Test
    public void getStringValue_Tags() throws IOException
    {
        NodeContext context = new NodeContext(new ParameterSet(null,null,invariants));
        FacetFieldAdapter target = new FacetFieldAdapter(context, "tags");

        SimpleOrderedMap<Object> resultBucket = new SimpleOrderedMap<>();
        resultBucket.add("val", "99^testtitle");
        String actual = target.getStringValue(resultBucket);

        Assert.assertEquals("99", actual);
    }

    @Test
    public void getMapValue_FieldWithDots() throws IOException
    {
        NodeContext context = new NodeContext(new ParameterSet(null,null,invariants));
        FacetFieldAdapter target = new FacetFieldAdapter(context, "field.with.dots");

        SimpleOrderedMap<Object> resultBucket = new SimpleOrderedMap<>();
        resultBucket.add("val", "99^testfieldvalue");
        SimpleOrderedMap<String> actual = target.getMapValue(resultBucket);

        Assert.assertEquals("99", actual.get("id"));
        Assert.assertEquals("testfieldvalue", actual.get("value"));
    }

    @Test
    public void getStringValue_FieldWithDots() throws IOException
    {
        NodeContext context = new NodeContext(new ParameterSet(null,null,invariants));
        FacetFieldAdapter target = new FacetFieldAdapter(context, "field.with.dots");

        SimpleOrderedMap<Object> resultBucket = new SimpleOrderedMap<>();
        resultBucket.add("val", "99^testvalue");
        String actual = target.getStringValue(resultBucket);

        Assert.assertEquals("99", actual);
    }

}