package com.careerbuilder.search.relevancy.waitable;

import org.apache.lucene.search.Query;
import org.apache.solr.search.DocSet;
import org.apache.solr.search.SolrIndexSearcher;

public class QueryWaitable extends Waitable {

    public enum QueryType {Q, FG, BG};
    public QueryType type;
    public int result = 0;
    public final int index;

    protected Query query;
    SolrIndexSearcher searcher;
    DocSet filter;
    Exception e;

    public QueryWaitable(SolrIndexSearcher searcher,
                         Query query, DocSet filter,
                         QueryType type,
                         int index) {
        this.query = query;
        this.searcher = searcher;
        this.filter = filter;
        this.type = type;
        this.index = index;
    }

    public Waitable call() {
        try {
            result = searcher.numDocs(query, filter);
        } catch (Exception e) {this.e = e; }
        return this;
    }

    public @Override boolean equals(Object other)
    {
        if(other instanceof QueryWaitable)
        {
            QueryWaitable instance = (QueryWaitable)other;
            return this.index == instance.index && this.type == instance.type;
        }
        return false;
    }
}
