package com.careerbuilder.search.relevancy;

import com.careerbuilder.search.relevancy.model.ParameterSet;
import com.careerbuilder.search.relevancy.model.KnowledgeGraphRequest;
import com.careerbuilder.search.relevancy.model.KnowledgeGraphResponse;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;
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


public class KnowledgeGraphHandler extends RequestHandlerBase
{
    private ResourceLoader loader;

    @Override
    public void init(NamedList args) {
        super.init(args);
    }

    @Override
    public void handleRequestBody(SolrQueryRequest solrReq, SolrQueryResponse solrRsp)
            throws Exception {
        KnowledgeGraphRequest request = parsePost(solrReq);
        new RequestValidator(solrReq, request).validate();
        ParameterSet parameterSet = new ParameterSet(solrReq.getParams(), defaults, invariants);
        NodeContext context = new NodeContext(request, solrReq, parameterSet);
        RequestTreeRecurser recurser = new RequestTreeRecurser(context);
        KnowledgeGraphResponse response = new KnowledgeGraphResponse();
        response.data = recurser.score();
        solrRsp.add("relatednessResponse", response);
    }

    private KnowledgeGraphRequest parsePost(SolrQueryRequest request) throws IOException {
        String inputString = getPostString(request);
        try {
            return new Gson().fromJson(inputString, KnowledgeGraphRequest.class);
        } catch (Exception e) {
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, e.getMessage());
        }
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
        if(inputString.equals("") || inputString == null) {
           throwWithClassName(" requires POST data");
        }
        return inputString;
    }

    private void throwWithClassName(String msgAfterClassName) {
        throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,
                getClass().getSimpleName()+ msgAfterClassName);
    }

    //////////////////////// SolrInfoMBeans methods //////////////////////

    @Override
    public String getDescription()
    {
        return "Ranks tags using corpus of tagged data";
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