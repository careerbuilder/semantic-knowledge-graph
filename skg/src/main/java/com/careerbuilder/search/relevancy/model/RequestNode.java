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

import org.apache.solr.common.util.SimpleOrderedMap;

import java.util.List;

public class RequestNode {
    public String type;
    public String [] values;
    public List<SimpleOrderedMap<String>> normalizedValues;
    public SortType sort;
    public int limit = -1;
    public boolean discover_values;
    public RequestNode[] compare;

    public RequestNode() {}

    public RequestNode(RequestNode[] compare,
                       String type) {
        this.type = type;
        this.compare = compare;
    }

}
