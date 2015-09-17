package com.careerbuilder.search.relevancy.Runnable;

import com.careerbuilder.search.relevancy.NodeContext;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.facet.FacetModule;

import java.io.IOException;
import java.util.*;

public class FacetRunner extends Waitable{


    private int limit;
    private String field;
    public List<SimpleOrderedMap<Object>> buckets;
    public String facetQuery;
    final NodeContext context;
    public static final String FIELD_FACET_NAME = "fieldFacet";
    public static final String QUERY_FACET_NAME = "queryFacet";

    public FacetRunner(NodeContext context, String facetQuery, String field, int limit) {
        this(context, field, limit);
        this.facetQuery = facetQuery;
    }

    public FacetRunner(NodeContext context, String field, int limit) {
        this.context = context;
        this.field = field;
        this.limit = limit;
    }

    public void run()
    {
        try {
            facet();
        } catch (Exception e) { this.e = e; }
        notifyCallers();
    }

    public void facet() throws IOException {
        FacetModule mod = new FacetModule();
        SolrQueryResponse resp = new SolrQueryResponse();
        ResponseBuilder rb = getResponseBuilder(resp);
        mod.prepare(rb);
        mod.process(rb);
        parseResponse(resp);
    }

    private ResponseBuilder getResponseBuilder(SolrQueryResponse resp) throws IOException {
        SolrQueryRequest req =  new LocalSolrQueryRequest(context.req.getCore(), buildFacetParams());
        req.setJSON(buildFacetJson());
        ResponseBuilder rb = new ResponseBuilder(req, resp, null);
        rb.setResults(context.queryDomainList);
        return rb;
    }

    // this is kind of awkward, but necessary since JsON faceting is protected in Solr,
    // (we can only interact with it by mocking up a request and parsing a response).
    private void parseResponse(SolrQueryResponse resp) {
        SimpleOrderedMap<Object> facet = (SimpleOrderedMap<Object>)
                ((SimpleOrderedMap<Object>)((SimpleOrderedMap<Object>) resp.getValues()).get("facets")).get(QUERY_FACET_NAME);
        if(facet != null) {
            SimpleOrderedMap<Object> innerFacet = (SimpleOrderedMap<Object>)facet.get(FIELD_FACET_NAME);
            if(innerFacet != null)
            {
                buckets = (List<SimpleOrderedMap<Object>>)innerFacet.get("buckets");
            } else {
                buckets = new LinkedList<>();
            }
        } else {
            buckets = new LinkedList<>();
        }
    }

    // see above
    public Map<String, Object> buildFacetJson()
    {
        int limit = 2*Math.max(this.limit, 25);
        LinkedHashMap<String, Object> wrapper = new LinkedHashMap<>();
        LinkedHashMap<String, Object> queryFacetName = new LinkedHashMap<>();
        LinkedHashMap<String, Object> queryFacetWrapper= new LinkedHashMap<>();
        LinkedHashMap<String, Object> queryFacet= new LinkedHashMap<>();
        LinkedHashMap<String, Object> fieldFacetName = new LinkedHashMap<>();
        LinkedHashMap<String, Object> fieldFacetWrapper= new LinkedHashMap<>();
        LinkedHashMap<String, Object> fieldFacet= new LinkedHashMap<>();
        fieldFacet.put("type", "field");
        fieldFacet.put("field", field);
        fieldFacet.put("limit", limit);
        fieldFacetWrapper.put("field", fieldFacet);
        fieldFacetName.put(FIELD_FACET_NAME, fieldFacetWrapper);
        queryFacet.put("facet", fieldFacetName);
        queryFacet.put("q", facetQuery);
        queryFacetWrapper.put("query", queryFacet);
        queryFacetName.put(QUERY_FACET_NAME, queryFacetWrapper);
        wrapper.put("facet", queryFacetName);
        return wrapper;
    }

    // see above
    public SolrParams buildFacetParams()
    {
        LinkedHashMap<String, String> paramMap = new LinkedHashMap<>();
        paramMap.put("facet.version", "1");
        paramMap.put("wt", "json");
        paramMap.put(FacetParams.FACET, "true");
        return new MapSolrParams(paramMap);
    }

}
