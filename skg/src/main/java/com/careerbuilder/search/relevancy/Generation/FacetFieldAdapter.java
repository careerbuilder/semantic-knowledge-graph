package com.careerbuilder.search.relevancy.Generation;

import com.careerbuilder.search.relevancy.NodeContext;
import org.apache.solr.common.SolrException;

public class FacetFieldAdapter {

    NodeContext context;

    public FacetFieldAdapter(NodeContext context)
    {
        this.context = context;
    }

    public String getFacetField(String field) {
        String extension = context.invariants.get(field + ".extension", "");
        String facetField = makeDefault(field, extension);
        return checkField(field, facetField);
    }

    private String makeDefault(String field, String extension) {
        StringBuilder facetField = new StringBuilder();
        if(extension.equals("")) {
            facetField.append(field);
        } else {
            int first = field.indexOf(".");
            int second = field.indexOf(".", first + 1);
            if (second > 0) {
                String suffix = field.substring(second);
                facetField.append(field.substring(0, second)).append(".").append(extension).append(suffix);
            } else {
                facetField.append(field).append(".").append(extension);
            }
        }
        return facetField.append(".cs").toString();
    }

    private String checkField(String inputField, String facetField) {
        try {
            facetField = verifyFieldInCore(facetField);
        } catch (SolrException e) {
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,
                    "Values of type \"" + inputField + "\" cannot be generated automatically" +
                    "(adapted as \"" + facetField + "\")");
        }
        return facetField;
    }

    private String verifyFieldInCore(String facetField) {
        facetField = context.req
                .getCore()
                .getLatestSchema()
                .getField(facetField).getName();
        return facetField;
    }
}
