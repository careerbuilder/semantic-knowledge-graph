package com.careerbuilder.search.relevancy.Generation;

import com.careerbuilder.search.relevancy.NodeContext;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.SimpleOrderedMap;

public class FacetFieldAdapter {

    NodeContext context;

    static final String FACET_FIELD_DELIMITER = "-";
    static final String FACET_FIELD_VALUE_DELIMITER= "^";

    public String field;
    public String baseField;
    private String facetFieldExtension;

    public FacetFieldAdapter(NodeContext context, String field)
    {

        this.context = context;
        this.facetFieldExtension = context.parameterSet.invariants.get(field + ".facet-field", "");
        checkField(field, field);
        this.baseField = field;
        this.field = buildField(field);
    }

    public SimpleOrderedMap<String> getMapValue(SimpleOrderedMap<Object> bucket) {
        SimpleOrderedMap<String> result = null;
        if(facetFieldExtension != null && !facetFieldExtension.equals("")) {
            result = new SimpleOrderedMap<>();
            String value = (String) bucket.get("val");
            String[] facetFieldKeys = facetFieldExtension.split(FACET_FIELD_DELIMITER);
            String[] facetFieldValues = value.split("\\"+FACET_FIELD_VALUE_DELIMITER);
            for (int i = 0; i < facetFieldKeys.length && i < facetFieldValues.length; ++i) {
                result.add(facetFieldKeys[i], facetFieldValues[i]);
            }
        }
        return result;
    }

    public String getStringValue(SimpleOrderedMap<Object> bucket)
    {
        String value = (String) bucket.get("val");
        return value.replace(FACET_FIELD_VALUE_DELIMITER, " ");
    }

    private String buildField(String field) {
        String facetField = extendField(field, facetFieldExtension);
        checkField(field, facetField);
        return facetField;
    }

    private String extendField(String field, String extension) {
        StringBuilder facetField = new StringBuilder();
        if(extension.equals("")) {
            facetField.append(field);
        } else {
            facetField.append(field).append(".").append(extension);
        }
        return facetField.append(".cs").toString();
    }

    public boolean hasExtension()
    {
        return facetFieldExtension != null && !facetFieldExtension.equals("");
    }

    private void checkField(String inputField, String facetField) {
        try {
            context.req.getCore()
                    .getLatestSchema()
                    .getField(facetField).getName();
        } catch (SolrException e) {
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,
                    "Values of type \"" + inputField + "\" cannot be generated automatically or normalized" +
                    "(adapted as \"" + facetField + "\")");
        }
    }
}
