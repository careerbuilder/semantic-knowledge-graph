package com.careerbuilder.search.relevancy;

import com.careerbuilder.search.relevancy.model.ParameterSet;
import com.careerbuilder.search.relevancy.model.KnowledgeGraphRequest;
import com.careerbuilder.search.relevancy.utility.ParseUtility;
import org.apache.lucene.search.Query;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.*;

import java.io.IOException;
import java.util.List;

public class NodeContext {

    public KnowledgeGraphRequest request;
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
    public NodeContext(KnowledgeGraphRequest request)
    {
        this.request = request;
    }

    @Deprecated
    public NodeContext(ParameterSet parameterSet)
    {
        this.parameterSet = parameterSet;
    }

    public NodeContext(KnowledgeGraphRequest request, SolrQueryRequest req, ParameterSet parameterSet) throws IOException
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
