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

import com.careerbuilder.search.relevancy.generation.NodeGenerator;
import com.careerbuilder.search.relevancy.model.RelatednessRequest;
import com.careerbuilder.search.relevancy.model.RequestNode;
import com.careerbuilder.search.relevancy.model.ResponseNode;
import com.careerbuilder.search.relevancy.model.ResponseValue;
import com.careerbuilder.search.relevancy.normalization.NodeNormalizer;
import com.careerbuilder.search.relevancy.scoring.NodeScorer;

import java.io.IOException;

public class RequestTreeRecurser {

    public static final int DEFAULT_REQUEST_LIMIT = 1;

    private RelatednessRequest request;
    private NodeContext baseContext;
    private RecursionOp normalizer;
    private RecursionOp generator;
    private RecursionOp scorer;

    public RequestTreeRecurser(NodeContext context) throws IOException {
        this(context, new NodeNormalizer(), new NodeGenerator(), new NodeScorer());
    }

    public RequestTreeRecurser(NodeContext context,
                               RecursionOp normalizer,
                               RecursionOp generator,
                               RecursionOp scorer) throws IOException {
        this.normalizer = normalizer;
        this.generator = generator;
        this.scorer = scorer;
        this.request = context.request;
        this.baseContext = context;
    }

    public ResponseNode[] score() throws IOException {
        ResponseNode[] responses = null;
        if(request.compare != null) {
            setDefaults(request.compare);
            if(baseContext.request.normalize) {
                normalizer.transform(baseContext, request.compare, null);
            }
            responses = generator.transform(baseContext, request.compare, null);
            responses = scorer.transform(baseContext, request.compare, responses);
            for(int i = 0; i < request.compare.length; ++i) {
                recurse(baseContext, responses[i], request.compare[i].compare);
            }
        }
        return responses;
    }

    private void recurse(NodeContext parentContext, ResponseNode parentResponse, RequestNode[] requests) throws IOException {
        if(requests != null) {
            setDefaults(requests);
            for (ResponseValue value : parentResponse.values) {
                String query = value.value == null || value.value.equals("") ? "*" : value.value.toLowerCase();
                NodeContext context = new NodeContext(parentContext, parentResponse.type+":"+query);
                if(context.request.normalize) {
                    normalizer.transform(context, requests, null);
                }
                ResponseNode [] responses = generator.transform(context, requests, null);
                value.compare = scorer.transform(context, requests, responses);
                for (int i = 0; i < requests.length; ++i) {
                    recurse(context, value.compare[i], requests[i].compare);
                }
            }
        }
    }

    private void setDefaults(RequestNode [] requests)
    {
        for(RequestNode request: requests) {
            if (request.values == null || request.values.length == 0) {
                request.discover_values = true;
            }
            int limit = request.values == null || request.values.length == 0 ? DEFAULT_REQUEST_LIMIT : request.values.length;
            request.limit = request.limit == -1 ? limit : request.limit;
        }
    }
}
