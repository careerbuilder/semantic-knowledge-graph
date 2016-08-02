package com.careerbuilder.search.relevancy.model;

public class ResponseNode {
    public String type;
    public ResponseValue[] values;

    public ResponseNode(){}

    public ResponseNode(String type) {
        this.type = type;
    }


}
