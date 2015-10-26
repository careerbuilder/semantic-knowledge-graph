package com.careerbuilder.search.relevancy;

import com.careerbuilder.search.relevancy.Generation.FacetFieldAdapter;
import com.careerbuilder.search.relevancy.Models.RelatednessRequest;
import com.careerbuilder.search.relevancy.Models.RequestNode;
import com.careerbuilder.search.relevancy.ResponseWriter.RelatednessResponseWriter;
import org.apache.solr.common.SolrException;

public class RequestValidator {

    private RelatednessRequest request;
    private NodeContext context;

    public RequestValidator(NodeContext context, RelatednessRequest request)
    {
        this.request = request;
        this.context = context;
    }

    public void validate()
    {
        if(request.compare == null || request.compare.length == 0) {
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Request contains no compare node or an empty compare node");
        }
        for(int i = 0; i < request.compare.length; ++i) {
            recurse(request.compare[i]);
        }
    }

    private void recurse(RequestNode requestNode)
    {
        if(requestNode.type == null || requestNode.type.equals("")) {
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "A request node contains empty or null type.");
        }
        FacetFieldAdapter.checkField(context, requestNode.type, requestNode.type);

        if(requestNode.compare != null) {
            for (int i = 0; i < requestNode.compare.length; ++i){
                recurse(requestNode.compare[i]);
            }
        }
    }
}
