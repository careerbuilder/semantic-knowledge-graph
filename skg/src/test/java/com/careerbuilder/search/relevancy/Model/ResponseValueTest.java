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

package com.careerbuilder.search.relevancy.model;

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
