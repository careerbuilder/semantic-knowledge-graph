package com.careerbuilder.search.relevancy;

import com.careerbuilder.search.relevancy.Models.RelatednessRequest;
import org.apache.lucene.search.Query;
import org.apache.solr.common.SolrException;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.*;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class NodeContext {

    public SolrIndexSearcher searcher;
    public SolrQueryRequest req;
    public List<Query> queries;
    public List<Query> fgQueries;
    public List<Query> bgQueries;
    public DocSet queryDomain;
    public DocSet fgDomain;
    public DocSet bgDomain;

    public NodeContext(RelatednessRequest request, SolrQueryRequest req) throws IOException
    {
        this.searcher = req.getSearcher();
        this.req = req;
        this.queries = parseQueryStrings(request.queries);
        this.fgQueries = parseQueryStrings(request.foreground_queries);
        this.bgQueries = parseQueryStrings(request.background_queries);
        this.fgQueries.addAll(bgQueries);
        this.queryDomain = searcher.getDocSet(queries);
        this.fgDomain= searcher.getDocSet(fgQueries);
        this.bgDomain = searcher.getDocSet(bgQueries);
    }

    // copy constructor
    public NodeContext(NodeContext parent, Query filterQuery) throws IOException
    {
        this.searcher = parent.searcher;
        this.req = parent.req;
        this.queries = parent.queries;
        this.fgQueries = parent.fgQueries;
        this.bgQueries = parent.bgQueries;
        this.queryDomain = searcher.getDocSet(filterQuery, parent.queryDomain);
        this.fgDomain= searcher.getDocSet(filterQuery, parent.fgDomain);
        this.bgDomain= searcher.getDocSet(filterQuery, parent.bgDomain);
    }

    public NodeContext() {}

    private List<Query> parseQueryStrings(String [] qStrings) {
        if(qStrings != null) {
            LinkedList<Query> queryList = new LinkedList<Query>();
            for (String qString : qStrings) {
                try {
                    QParser parser = QParser.getParser(qString, null, req);
                    queryList.add(parser.getQuery());
                } catch (SyntaxError e) {
                    throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,
                            "Syntax error in query: " + qString + ".");
                }
            }
            return queryList;
        }
        return null;
    }
}
