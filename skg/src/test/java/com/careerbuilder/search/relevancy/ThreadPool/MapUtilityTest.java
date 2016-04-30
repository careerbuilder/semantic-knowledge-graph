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

package com.careerbuilder.search.relevancy.threadpool;

import com.careerbuilder.search.relevancy.utility.MapUtility;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MapUtilityTest {


    @Before
    public void init() {
    }

    @Test
    public void mapContainsValue()
    {
        SimpleOrderedMap<String> map = new SimpleOrderedMap<>();
        map.add("id", "123");
        map.add("name", "testName");

        Assert.assertTrue(MapUtility.mapContainsValue("123", map));
        Assert.assertFalse(MapUtility.mapContainsValue("12", map));
        Assert.assertTrue(MapUtility.mapContainsValue("testName", map));
        Assert.assertFalse(MapUtility.mapContainsValue("test", map));
    }

    @Test public void mapsEqual() {
        SimpleOrderedMap<String> map1 = new SimpleOrderedMap<>();
        map1.add("id", "123");
        map1.add("name", "testName");
        SimpleOrderedMap<String> map2 = new SimpleOrderedMap<>();
        map2.add("id", "123");
        map2.add("name", "testName");

        Assert.assertTrue(MapUtility.mapsEqual(map1, map2));
    }

    @Test public void mapsEqual_NotEqual() {
        SimpleOrderedMap<String> map1 = new SimpleOrderedMap<>();
        map1.add("id", "123");
        map1.add("name", "testName");
        SimpleOrderedMap<String> map2 = new SimpleOrderedMap<>();
        map2.add("id", "321");
        map2.add("name", "testName");
        SimpleOrderedMap<String> emptyMap = new SimpleOrderedMap<>();

        Assert.assertFalse(MapUtility.mapsEqual(map1, map2));
        Assert.assertFalse(MapUtility.mapsEqual(null, map2));
        Assert.assertFalse(MapUtility.mapsEqual(map1, null));
        Assert.assertFalse(MapUtility.mapsEqual(map1, emptyMap));
        Assert.assertFalse(MapUtility.mapsEqual(emptyMap, map2));
    }

}
