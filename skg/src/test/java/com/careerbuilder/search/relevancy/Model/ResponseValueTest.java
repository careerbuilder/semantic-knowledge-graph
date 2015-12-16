package com.careerbuilder.search.relevancy.Model;

import com.careerbuilder.search.relevancy.Models.ResponseValue;
import mockit.integration.junit4.JMockit;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class ResponseValueTest {

    @Test
    public void extendedEquals_equals()
    {
        ResponseValue r1 = new ResponseValue("test1", 1.0);
        ResponseValue r2 = new ResponseValue("12 test1", 1.0);

        r2.normalizedValue = new SimpleOrderedMap<String>();
        r2.normalizedValue.add("testkey1", "12");
        r2.normalizedValue.add("testkey2", "test1");

        Assert.assertTrue(r1.expandedEquals(r2));
        Assert.assertTrue(r2.expandedEquals(r1));
    }

    @Test
    public void extendedEquals_magnitudeDifferent()
    {
        ResponseValue r1 = new ResponseValue("test1", 1.0);
        ResponseValue r2 = new ResponseValue("12 test1", 1.1);

        r2.normalizedValue = new SimpleOrderedMap<String>();
        r2.normalizedValue.add("testkey1", "12");
        r2.normalizedValue.add("testkey2", "test1");

        Assert.assertTrue(!r1.expandedEquals(r2));
        Assert.assertTrue(!r2.expandedEquals(r1));
    }

    @Test
    public void valuesEqual_equal()
    {
        ResponseValue r1 = new ResponseValue("test1", 1.0);
        ResponseValue r2 = new ResponseValue("12 test1", 1.0);

        r2.normalizedValue = new SimpleOrderedMap<>();
        r2.normalizedValue.add("testkey1", "12");
        r2.normalizedValue.add("testkey2", "test1");

        Assert.assertTrue(r1.valuesEqual(r2));
        Assert.assertTrue(r2.valuesEqual(r1));
    }

    @Test
    public void valuesEqual_equalUID()
    {
        ResponseValue r1 = new ResponseValue("12 test1", 1.0);
        ResponseValue r2 = new ResponseValue("12 test1", 1.0);

        r2.normalizedValue = new SimpleOrderedMap<>();
        r2.normalizedValue.add("testkey1", "12");
        r2.normalizedValue.add("testkey2", "test1");

        Assert.assertTrue(r1.valuesEqual(r2));
        Assert.assertTrue(r2.valuesEqual(r1));
    }


    @Test
    public void valuesEqual_notEqual()
    {
        ResponseValue r1 = new ResponseValue("13.1 test1", 1.0);
        ResponseValue r2 = new ResponseValue("12 test1", 1.0);

        r2.normalizedValue = new SimpleOrderedMap<>();
        r2.normalizedValue.add("testkey1", "12");
        r2.normalizedValue.add("testkey2", "test1");

        Assert.assertTrue(!r1.valuesEqual(r2));
        Assert.assertTrue(!r2.valuesEqual(r1));
    }

}
