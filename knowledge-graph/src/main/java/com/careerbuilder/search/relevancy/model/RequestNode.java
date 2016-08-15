package com.careerbuilder.search.relevancy.model;

import org.apache.solr.common.util.SimpleOrderedMap;

import java.util.List;

public class RequestNode {
    public String type;
    public String [] values;
    public List<SimpleOrderedMap<String>> normalizedValues;
    public SortType sort;
    public int limit;
    public boolean discover_values;
    public RequestNode[] compare;

    public RequestNode() {}

    public RequestNode(RequestNode[] compare,
                       String type) {
        this.type = type;
        this.compare = compare;
    }

}
