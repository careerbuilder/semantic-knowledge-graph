package com.careerbuilder.search.relevancy.Generation;

import com.careerbuilder.search.relevancy.Models.ResponseValue;
import com.careerbuilder.search.relevancy.NodeContext;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.SimpleOrderedMap;

public class FacetFieldAdapter {

    NodeContext context;

    static final String FACET_FIELD_DELIMITER = "-";
    static final String FACET_FIELD_VALUE_DELIMITER= "^";

    public String field;
    private String facetFieldExtension;

    public FacetFieldAdapter(NodeContext context, String field)
    {
        this.context = context;
        this.facetFieldExtension = context.parameterSet.invariants.get(field + ".facet-field", "");
        this.field = buildFacetField(field);
    }

    public ResponseValue buildResponseValue(SimpleOrderedMap<Object> bucket) {
        String value = (String) bucket.get("val");
        ResponseValue resp = new ResponseValue(value.replace(FACET_FIELD_VALUE_DELIMITER, " "));
        if(facetFieldExtension != null && !facetFieldExtension.equals("")) {
            resp.facetValue = new SimpleOrderedMap<String>();
            String[] facetFieldKeys = facetFieldExtension.split(FACET_FIELD_DELIMITER);
            String[] facetFieldValues = value.split("\\"+FACET_FIELD_VALUE_DELIMITER);
            for (int i = 0; i < facetFieldKeys.length && i < facetFieldValues.length; ++i) {
                resp.facetValue.add(facetFieldKeys[i], facetFieldValues[i]);
            }
        }
        return resp;
    }

    private String buildFacetField(String field) {
        String facetField = makeDefault(field, facetFieldExtension);
        checkField(field, facetField);
        return facetField;
    }

    // to get the facet field:
    // append the facet field extension if it exists, then .cs
    private String makeDefault(String field, String extension) {
        StringBuilder facetField = new StringBuilder();
        if(extension.equals("")) {
            facetField.append(field);
        } else {
            facetField.append(field).append(".").append(extension);
        }
        return facetField.append(".cs").toString();
    }

    private void checkField(String inputField, String facetField) {
        try {
            context.req.getCore()
                    .getLatestSchema()
                    .getField(facetField).getName();
        } catch (SolrException e) {
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,
                    "Values of type \"" + inputField + "\" cannot be generated automatically" +
                    "(adapted as \"" + facetField + "\")");
        }
    }
}
