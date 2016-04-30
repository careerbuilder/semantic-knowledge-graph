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

import com.careerbuilder.search.relevancy.model.ParameterSet;
import com.careerbuilder.search.relevancy.model.RelatednessRequest;
import com.careerbuilder.search.relevancy.utility.ParseUtility;
import org.apache.lucene.search.Query;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.*;

import java.io.IOException;
import java.util.List;

public class NodeContext {

    public RelatednessRequest request;
    public SolrQueryRequest req;
    public ParameterSet parameterSet;
    public List<Query> queries;
    public List<Query> fgQueries;
    public List<Query> bgQueries;
    public DocListAndSet queryDomainList;
    public DocSet queryDomain;
    public DocSet fgDomain;
    public DocSet bgDomain;

    @Deprecated
    public NodeContext(RelatednessRequest request)
    {
        this.request = request;
    }

    @Deprecated
    public NodeContext(ParameterSet parameterSet)
    {
        this.parameterSet = parameterSet;
    }

    public NodeContext(RelatednessRequest request, SolrQueryRequest req, ParameterSet parameterSet) throws IOException
    {
        this.request = request;
        this.req = req;
        this.parameterSet = parameterSet;
        this.queries = ParseUtility.parseQueryStrings(request.queries, req);
        if(request.foreground_queries == null) {
            this.fgQueries = this.queries;
        }
        else {
            this.fgQueries = ParseUtility.parseQueryStrings(request.foreground_queries, req);
        }
        this.bgQueries = ParseUtility.parseQueryStrings(request.background_queries, req);
        this.queryDomain = req.getSearcher().getDocSet(queries);
        this.fgDomain= req.getSearcher().getDocSet(fgQueries);
        this.bgDomain = req.getSearcher().getDocSet(bgQueries);
    }

    // copy constructor
    public NodeContext(NodeContext parent, String filterQueryString) throws IOException
    {
        this.req = parent.req;
        this.request = parent.request;
        this.parameterSet = parent.parameterSet;
        this.queries = parent.queries;
        this.fgQueries = parent.fgQueries;
        this.bgQueries = parent.bgQueries;
        this.queryDomain = req.getSearcher().getDocSet(ParseUtility.parseQueryString(filterQueryString, req), parent.queryDomain);
        this.fgDomain= req.getSearcher().getDocSet(ParseUtility.parseQueryString(filterQueryString, req), parent.fgDomain);
        this.bgDomain= parent.bgDomain;
    }

    public NodeContext() {}


}
