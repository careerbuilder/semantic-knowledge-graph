package com.careerbuilder.search.relevancy.ResponseWriter;

import com.careerbuilder.search.relevancy.Models.ResponseValue;
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

        System.out.println(json);
        Assert.assertEquals("{\"name\":\"12 test1\",\"relatedness\":0.0,\"popularity\":0.0,\"foreground_popularity\":1.0,\"background_popularity\":0.0}",
                json);
    }

}
