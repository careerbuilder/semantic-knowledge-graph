package com.careerbuilder.search.relevancy.responsewriter;

import com.careerbuilder.search.relevancy.model.*;
import com.careerbuilder.search.relevancy.model.Error;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
        Gson gson = new GsonBuilder().registerTypeAdapter(ResponseValue.class, new ResponseValueSerializer()).create();
        Exception e = response.getException();
        int status = (int)response.getResponseHeader().get("status");
        RelatednessResponse model = (RelatednessResponse)response.getValues().get("relatednessResponse");
        if(e != null) {
            if(model == null) {
                model = new RelatednessResponse();
            }
            model.error = new Error(e.getMessage(), status);
        }
        writer.write(gson.toJson(model, RelatednessResponse.class));
    }

}
