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
