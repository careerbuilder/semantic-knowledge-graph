package com.careerbuilder.search.relevancy.utility;

import org.apache.solr.common.util.SimpleOrderedMap;

public class MapUtility {
    public static boolean mapContainsValue(String value, SimpleOrderedMap<String> normValues) {
        if(normValues != null) {
            for(int i = 0; i < normValues.size(); ++i) {
                if(normValues.getVal(i).equalsIgnoreCase(value))
                    return true;
            }
        }
        return false;
    }

    public static boolean mapsEqual(SimpleOrderedMap<String> map1, SimpleOrderedMap<String> map2) {
        if(map1 != null && map2 != null) {
            if(map1.size() != map2.size()) {
                return false;
            }
            for (int i = 0; i < map1.size(); ++i) {
                if (!map1.getVal(i).equals(map2.getVal(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
