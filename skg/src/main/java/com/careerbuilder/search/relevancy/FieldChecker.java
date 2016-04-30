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
