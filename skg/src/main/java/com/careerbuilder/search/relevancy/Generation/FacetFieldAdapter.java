/**Copyright 2015-2016 CareerBuilder, LLC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
**/

package com.careerbuilder.search.relevancy.generation;

import com.careerbuilder.search.relevancy.FieldChecker;
import com.careerbuilder.search.relevancy.NodeContext;
import org.apache.solr.common.util.SimpleOrderedMap;

public class FacetFieldAdapter {

    NodeContext context;


    public String field;
    public String baseField;
    private String facetFieldExtension;
    private String facetFieldKey;
    private String globalFacetFieldExtension;
    private String facetFieldDelimiter= "-";
    private String facetFieldValueDelimiter = "^";

    @Deprecated
    public FacetFieldAdapter(String field) {
        this.field = field;
        this.baseField = field;
    }

    public FacetFieldAdapter(NodeContext context, String field)
    {
        this.context = context;
        this.facetFieldExtension = context.parameterSet.invariants.get(field + ".facet-field", "");
        this.facetFieldKey = context.parameterSet.invariants.get(field + ".key", "");
        this.globalFacetFieldExtension = context.parameterSet.invariants.get("facet-field-extension", "cs");
        this.facetFieldDelimiter = context.parameterSet.invariants.get("facet-field-delimiter", "-");
        this.facetFieldValueDelimiter = context.parameterSet.invariants.get("facet-field-value-delimiter", "^");
        FieldChecker.checkField(context.req, field, field);
        this.baseField = field;
        this.field = buildField(field);
    }

    public SimpleOrderedMap<String> getMapValue(SimpleOrderedMap<Object> bucket) {
        SimpleOrderedMap<String> result = null;
        if(facetFieldExtension != null && !facetFieldExtension.equals("")) {
            result = new SimpleOrderedMap<>();
            String value = (String) bucket.get("val");
            String[] facetFieldKeys = facetFieldExtension.split(facetFieldDelimiter);
            String[] facetFieldValues = value.split("\\"+facetFieldValueDelimiter);
            for (int i = 0; i < facetFieldKeys.length && i < facetFieldValues.length; ++i) {
                if(!facetFieldValues.equals("")) {
                    result.add(facetFieldKeys[i], facetFieldValues[i]);
                }
            }
        }
        return result;
    }

    public String getStringValue(SimpleOrderedMap<Object> bucket)
    {
        SimpleOrderedMap<String> mapValues = getMapValue(bucket);
        if(mapValues == null)
        {
            return (String) bucket.get("val");
        }
        return mapValues.get(this.facetFieldKey);
    }

    private String buildField(String field) {
        String facetField = extendField(field, facetFieldExtension);
        FieldChecker.checkField(context.req, field, facetField);
        return facetField;
    }

    private String extendField(String field, String extension) {
        StringBuilder facetField = new StringBuilder();
        if(extension.equals("")) {
            facetField.append(field);
        } else {
            facetField.append(field).append(".").append(extension);
        }
        return facetField.append(".").append(globalFacetFieldExtension).toString();
    }

    public boolean hasExtension()
    {
        return facetFieldExtension != null && !facetFieldExtension.equals("");
    }


}
