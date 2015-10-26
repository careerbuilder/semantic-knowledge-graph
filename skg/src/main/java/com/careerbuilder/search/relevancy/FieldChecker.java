package com.careerbuilder.search.relevancy;

import org.apache.solr.common.SolrException;

public class FieldChecker {

    public static void checkField(NodeContext context, String inputField, String facetField) {
        try {
            context.req.getCore()
                    .getLatestSchema()
                    .getField(facetField).getName();
        } catch (SolrException e) {
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,
                    "Values of type \"" + inputField + "\" cannot be generated automatically or normalized " +
                            "(adapted as \"" + facetField + "\")");
        }
    }
}
