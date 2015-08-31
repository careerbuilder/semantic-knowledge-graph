package com.careerbuilder.search.relevancy.ResponseWriter;

import com.google.gson.Gson;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.QueryResponseWriter;
import org.apache.solr.response.SolrQueryResponse;

import java.io.IOException;
import java.io.Writer;

public class RelatednessResponseWriter implements QueryResponseWriter{

    public void init(NamedList args)
    {

    }
    public String getContentType(SolrQueryRequest request, SolrQueryResponse response)
    {
        return "application/json";
    }

    public void write(Writer writer, SolrQueryRequest request, SolrQueryResponse response) throws IOException
    {
        Gson gson = new Gson();
        Exception e = response.getException();
        int status = (int)response.getResponseHeader().get("status");
        if(e == null) {
            writer.write(gson.toJson(response.getValues().get("relatednessResponse")));
        }
        else {
            ErrorResponse error = new ErrorResponse(new ResponseHeader(status), new Error(e.getMessage(),status));
            writer.write(gson.toJson(error));
        }
    }

    public class Error
    {
        public String msg;
        public int code;

        public Error(String msg, int code)
        {
            this.msg = msg;
            this.code = code;
        }
    }

    public class ResponseHeader
    {
        public int status;

        public ResponseHeader(int status)
        {
            this.status = status;
        }
    }

    public class ErrorResponse
    {
        public ResponseHeader responseHeader;
        public Error error;

        public ErrorResponse(ResponseHeader header, Error error)
        {
            this.responseHeader = header;
            this.error = error;
        }

    }
}
