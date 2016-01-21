package com.careerbuilder.search.relevancy.model;

public class Error {
    public String msg;
    public int code;

    public Error(String msg, int code)
    {
        this.msg = msg;
        this.code = code;
    }
}
