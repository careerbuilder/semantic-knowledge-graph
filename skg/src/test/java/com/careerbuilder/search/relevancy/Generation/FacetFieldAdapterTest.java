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
                    "carotene.v1",
                    "carotene.v1.top",
                    "carotene.v2",
                    "carotene.v2.top",
                    "carotene.v2_2",
                    "carotene.v2_2.top",
                    "onet.v15",
                    "onet.v15.top",
                    "onet.v17",
                    "onet.v17.top",
                    "skills.v2",
                    "skills.v2.top",
                    "skills.v3",
                    "skills.v3.top",
                    "joblevel.v1",
                    "carotene.v1.id-title.cs",
                    "carotene.v1.top.id-title.cs",
                    "carotene.v2.id-title.cs",
                    "carotene.v2.top.id-title.cs",
                    "carotene.v2_2.id-title.cs",
                    "carotene.v2_2.top.id-title.cs",
                    "onet.v15.id-title.cs",
                    "onet.v15.top.id-title.cs",
                    "onet.v17.id-title.cs",
                    "onet.v17.top.id-title.cs",
                    "skills.v2.did-title.cs",
                    "skills.v2.top.did-title.cs",
                    "skills.v3.did-title.cs",
                    "skills.v3.top.did-title.cs",
                    "joblevel.v1.level-description.cs",
            };

    @Before
    public void init() {
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("carotene.v1.facet-field", "id-title");
        paramMap.put("carotene.v1.top.facet-field", "id-title");
        paramMap.put("carotene.v2.facet-field", "id-title");
        paramMap.put("carotene.v2.top.facet-field", "id-title");
        paramMap.put("carotene.v2_2.facet-field", "id-title");
        paramMap.put("carotene.v2_2.top.facet-field", "id-title");
        paramMap.put("onet.v15.facet-field", "id-title");
        paramMap.put("onet.v15.top.facet-field", "id-title");
        paramMap.put("onet.v17.facet-field", "id-title");
        paramMap.put("onet.v17.top.facet-field", "id-title");
        paramMap.put("skills.v2.facet-field", "id-title");
        paramMap.put("skills.v2.top.facet-field", "id-title");
        paramMap.put("skills.v3.facet-field", "id-title");
        paramMap.put("skills.v3.top.facet-field", "id-title");
        paramMap.put("joblevel.v1.facet-field", "level-description");
        paramMap.put("carotene.v1.key", "id");
        paramMap.put("carotene.v1.top.key", "id");
        paramMap.put("carotene.v2.key", "id");
        paramMap.put("carotene.v2.top.key", "id");
        paramMap.put("carotene.v2_2.key", "id");
        paramMap.put("carotene.v2_2.top.key", "id");
        paramMap.put("onet.v15.key", "id");
        paramMap.put("onet.v15.top.key", "id");
        paramMap.put("onet.v17.key", "id");
        paramMap.put("onet.v17.top.key", "id");
        paramMap.put("skills.v2.key", "id");
        paramMap.put("skills.v2.top.key", "id");
        paramMap.put("skills.v3.key", "id");
        paramMap.put("skills.v3.top.key", "id");
        paramMap.put("joblevel.v1.key", "level");

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
        FacetFieldAdapter target = new FacetFieldAdapter(context, "carotene");
    }


    @Test
    public void testAdaptCaroteneV1() throws IOException
    {
        NodeContext context = new NodeContext(new ParameterSet(null, null, invariants));
        FacetFieldAdapter target = new FacetFieldAdapter(context, "carotene.v1");

        String actual = target.field;
        Assert.assertEquals("carotene.v1.id-title.cs", actual);
    }

    @Test
    public void testAdaptCaroteneV1Top()
    {
        NodeContext context = new NodeContext(new ParameterSet(null, null, invariants));
        FacetFieldAdapter target = new FacetFieldAdapter(context, "carotene.v1.top");

        String actual = target.field;
        Assert.assertEquals("carotene.v1.top.id-title.cs", actual);
    }

    @Test
    public void testAdaptJobLevel() throws IOException
    {
        NodeContext context = new NodeContext(new ParameterSet(null,null,invariants));
        FacetFieldAdapter target = new FacetFieldAdapter(context, "joblevel.v1");

        String actual = target.field;
        Assert.assertEquals("joblevel.v1.level-description.cs", actual);
    }


    @Test
    public void getMapValue_JobLevel() throws IOException
    {
        NodeContext context = new NodeContext(new ParameterSet(null, null, invariants));
        FacetFieldAdapter target = new FacetFieldAdapter(context, "joblevel.v1");

        SimpleOrderedMap<Object> resultBucket = new SimpleOrderedMap<>();
        resultBucket.add("val", "99^testdescription");
        SimpleOrderedMap<String> actual = target.getMapValue(resultBucket);

        Assert.assertEquals("99", actual.get("level"));
        Assert.assertEquals("testdescription", actual.get("description"));
    }

    @Test
    public void getStringValue_JobLevel() throws IOException
    {
        NodeContext context = new NodeContext(new ParameterSet(null,null,invariants));
        FacetFieldAdapter target = new FacetFieldAdapter(context, "joblevel.v1");

        SimpleOrderedMap<Object> resultBucket = new SimpleOrderedMap<>();
        resultBucket.add("val", "99^testdescription");
        String actual = target.getStringValue(resultBucket);

        Assert.assertEquals("99", actual);
    }

    @Test
    public void getMapValue_Carotene() throws IOException
    {
        NodeContext context = new NodeContext(new ParameterSet(null,null,invariants));
        FacetFieldAdapter target = new FacetFieldAdapter(context, "carotene.v1");

        SimpleOrderedMap<Object> resultBucket = new SimpleOrderedMap<>();
        resultBucket.add("val", "99^testtitle");
        SimpleOrderedMap<String> actual = target.getMapValue(resultBucket);

        Assert.assertEquals("99", actual.get("id"));
        Assert.assertEquals("testtitle", actual.get("title"));
    }

    @Test
    public void getStringValue_Carotene() throws IOException
    {
        NodeContext context = new NodeContext(new ParameterSet(null,null,invariants));
        FacetFieldAdapter target = new FacetFieldAdapter(context, "carotene.v1");

        SimpleOrderedMap<Object> resultBucket = new SimpleOrderedMap<>();
        resultBucket.add("val", "99^testtitle");
        String actual = target.getStringValue(resultBucket);

        Assert.assertEquals("99", actual);
    }

    @Test
    public void getMapValue_Skills() throws IOException
    {
        NodeContext context = new NodeContext(new ParameterSet(null,null,invariants));
        FacetFieldAdapter target = new FacetFieldAdapter(context, "skills.v2");

        SimpleOrderedMap<Object> resultBucket = new SimpleOrderedMap<>();
        resultBucket.add("val", "99^testtitle");
        SimpleOrderedMap<String> actual = target.getMapValue(resultBucket);

        Assert.assertEquals("99", actual.get("id"));
        Assert.assertEquals("testtitle", actual.get("title"));
    }


    @Test
    public void getStringValue_Skills() throws IOException
    {
        NodeContext context = new NodeContext(new ParameterSet(null,null,invariants));
        FacetFieldAdapter target = new FacetFieldAdapter(context, "skills.v2");

        SimpleOrderedMap<Object> resultBucket = new SimpleOrderedMap<>();
        resultBucket.add("val", "99^testtitle");
        String actual = target.getStringValue(resultBucket);

        Assert.assertEquals("99", actual);
    }

}
