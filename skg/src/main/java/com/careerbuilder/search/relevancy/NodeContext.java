package com.careerbuilder.search.relevancy;

import com.careerbuilder.search.relevancy.Models.ParameterSet;
import com.careerbuilder.search.relevancy.Models.RelatednessRequest;
import org.apache.lucene.search.Query;
import org.apache.solr.common.SolrException;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.*;

import java.io.IOException;
import java.util.LinkedList;
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
        this.queries = parseQueryStrings(request.queries);
        if(request.foreground_queries == null) {
            this.fgQueries = this.queries;
        }
        else {
            this.fgQueries = parseQueryStrings(request.foreground_queries);
        }
        this.bgQueries = parseQueryStrings(request.background_queries);
        this.fgQueries.addAll(bgQueries);
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
        this.queryDomain = req.getSearcher().getDocSet(parseQueryString(filterQueryString), parent.queryDomain);
        this.fgDomain= req.getSearcher().getDocSet(parseQueryString(filterQueryString), parent.fgDomain);
        this.bgDomain= parent.bgDomain;
    }

    public NodeContext() {}

    private List<Query> parseQueryStrings(String [] qStrings) {
        if(qStrings != null) {
            LinkedList<Query> queryList = new LinkedList<Query>();
            for (String qString : qStrings) {
                queryList.add(parseQueryString(qString));
            }
            return queryList;
        }
        return null;
    }

    private Query parseQueryString(String qString) {
        try {
            QParser parser = QParser.getParser(qString, null, req);
            return parser.getQuery();
        } catch (SyntaxError e) {
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,
                    "Syntax error in query: " + qString + ".");
        }
    }
}
