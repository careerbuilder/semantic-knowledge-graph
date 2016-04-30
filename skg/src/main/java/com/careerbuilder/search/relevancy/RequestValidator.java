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

import com.careerbuilder.search.relevancy.model.RelatednessRequest;
import com.careerbuilder.search.relevancy.model.RequestNode;
import org.apache.solr.common.SolrException;
import org.apache.solr.request.SolrQueryRequest;

public class RequestValidator {

    private RelatednessRequest request;
    private SolrQueryRequest solrRequest;

    public RequestValidator(SolrQueryRequest solrRequest, RelatednessRequest request)
    {
        this.request = request;
        this.solrRequest= solrRequest;
    }

    public void validate()
    {
        if(request.queries == null) {
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "No queries supplied for generation / scoring");
        }
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
        FieldChecker.checkField(solrRequest, requestNode.type, requestNode.type);

        if(requestNode.compare != null) {
            for (int i = 0; i < requestNode.compare.length; ++i){
                recurse(requestNode.compare[i]);
            }
        }
    }
}
