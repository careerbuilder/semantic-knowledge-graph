package com.careerbuilder.search.relevancy;

import org.apache.solr.common.SolrException;
import org.apache.solr.request.SolrQueryRequest;

public class FieldChecker {

    public static void checkField(SolrQueryRequest req, String inputField, String facetField) {
        try {
            req.getCore()
                    .getLatestSchema()
                    .getField(facetField).getName();
        } catch (SolrException e) {
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,
                    "Values of type \"" + inputField + "\" cannot be generated automatically or normalized " +
                            "(adapted as \"" + facetField + "\")");
        }
    }
}
