package com.careerbuilder.search.relevancy.model;

import org.apache.solr.common.params.SolrParams;

public class ParameterSet {

    public ParameterSet(SolrParams params, SolrParams defaults, SolrParams invariants)
    {
        this.params = params;
        this.defaults = defaults;
        this.invariants = invariants;
    }
    public SolrParams params;
    public SolrParams defaults;
    public SolrParams invariants;
}
