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

package com.careerbuilder.search.relevancy.responsewriter;

import com.careerbuilder.search.relevancy.model.ResponseValue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mockit.integration.junit4.JMockit;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class ResponseValueSerializerTest {

    @Test
    public void serialize_facetValue()
    {
        ResponseValue r1 = new ResponseValue("12 test1", 1.0);
        r1.normalizedValue = new SimpleOrderedMap<>();
        r1.normalizedValue.add("id", "12");
        r1.normalizedValue.add("title", "test1");

        Gson gson = new GsonBuilder().registerTypeAdapter(ResponseValue.class, new ResponseValueSerializer()).create();

        String json = gson.toJson(r1);

        Assert.assertEquals("{\"id\":\"12\",\"title\":\"test1\",\"relatedness\":0.0,\"popularity\":0.0,\"foreground_popularity\":1.0,\"background_popularity\":0.0}",
                json);
    }

    @Test
    public void serialize_value()
    {
        ResponseValue r1 = new ResponseValue("12 test1", 1.0);

        Gson gson = new GsonBuilder().registerTypeAdapter(ResponseValue.class, new ResponseValueSerializer()).create();

        String json = gson.toJson(r1);

        Assert.assertEquals("{\"name\":\"12 test1\",\"relatedness\":0.0,\"popularity\":0.0,\"foreground_popularity\":1.0,\"background_popularity\":0.0}",
                json);
    }

}
