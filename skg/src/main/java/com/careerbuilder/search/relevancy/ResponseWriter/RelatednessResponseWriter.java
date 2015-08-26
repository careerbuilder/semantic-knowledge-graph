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
        writer.write(new Gson().toJson(response.getValues().get("relatednessResponse")));
    }

}
