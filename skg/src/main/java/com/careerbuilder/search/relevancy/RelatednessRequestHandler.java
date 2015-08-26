package com.careerbuilder.search.relevancy;

import com.careerbuilder.search.relevancy.Models.RelatednessRequest;
import com.careerbuilder.search.relevancy.Models.ResponseNode;
import com.careerbuilder.search.relevancy.Models.ResponseValue;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import jdk.nashorn.internal.ir.RuntimeNode;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;


public class RelatednessRequestHandler extends RequestHandlerBase
{
    private ResourceLoader loader;

    @Override
    public void init(NamedList args) {
        super.init(args);
    }

    @Override
    public void handleRequestBody(SolrQueryRequest solrReq, SolrQueryResponse solrRsp)
            throws Exception {
        RelatednessRequest request = parsePost(solrReq);
        RequestTreeRecurser recurser = new RequestTreeRecurser(request, solrReq);
        solrRsp.add("relatednessResponse", recurser.score());
    }

    private RelatednessRequest parsePost(SolrQueryRequest request) throws IOException {
        String inputString = getPostString(request);
        return new Gson().fromJson(inputString, RelatednessRequest.class);
    }

    private String getPostString(SolrQueryRequest request) throws IOException {
        Reader inputReader = null;
        Iterable<ContentStream> streams = request.getContentStreams();
        if (streams != null) {
            Iterator<ContentStream> iter = streams.iterator();
            if (iter.hasNext()) {
                inputReader = iter.next().getReader();
            }
            if (iter.hasNext()) {
                throwWithClassName(" does not support multiple ContentStreams");
            }
        }
        if (inputReader == null) {
            throwWithClassName(" requires POST data");
        }
        String inputString;
        inputString = CharStreams.toString(inputReader);
        inputReader.close();
        return inputString;
    }

    private void throwWithClassName(String msgAfterClassName) {
        throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,
                getClass().getSimpleName()+ msgAfterClassName);
    }

    //////////////////////// SolrInfoMBeans methods //////////////////////

    @Override
    public String getDescription() {
        return "Parses a phrase";
    }

    @Override
    public String getSource() {
        return "$Source$";
    }

    @Override
    public String getVersion() {
        return "$Revision$";
    }


}