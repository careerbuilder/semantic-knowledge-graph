package com.careerbuilder.search.relevancy.Runnable;

import org.apache.lucene.search.Query;
import org.apache.solr.search.DocSet;
import org.apache.solr.search.SolrIndexSearcher;

public class QueryRunner extends Waitable {

    protected Query query;
    SolrIndexSearcher searcher;
    DocSet filter;
    Exception e;
    public int result = 0;

    public QueryRunner(SolrIndexSearcher searcher, Query query, DocSet filter) {
        this.query = query;
        this.searcher = searcher;
        this.filter = filter;
    }

    public void run() {
        try {
            result = searcher.numDocs(query, filter);
        } catch (Exception e) {this.e = e; }
        notifyCallers();
    }

}
