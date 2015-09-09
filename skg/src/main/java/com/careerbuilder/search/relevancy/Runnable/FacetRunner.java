package com.careerbuilder.search.relevancy.Runnable;

import com.careerbuilder.search.relevancy.Generation.FacetFieldAdapter;
import com.careerbuilder.search.relevancy.Models.RequestNode;
import com.careerbuilder.search.relevancy.Models.ResponseValue;
import com.careerbuilder.search.relevancy.NodeContext;
import com.google.gson.Gson;
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


    final NodeContext context;
    RequestNode requestNode;
    public List<ResponseValue> results;
    private FacetFieldAdapter adapter;
    Gson gson;

    public FacetRunner(NodeContext context, RequestNode requestNode) {
        this.gson = new Gson();
        this.requestNode = requestNode;
        this.context = context;
        adapter = new FacetFieldAdapter(context, requestNode.type);
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
        results = parseResponse(resp);
    }

    private ResponseBuilder getResponseBuilder(SolrQueryResponse resp) throws IOException {
        SolrQueryRequest req =  new LocalSolrQueryRequest(context.req.getCore(), buildFacetParams());
        req.setJSON(buildFacetJson());
        ResponseBuilder rb = new ResponseBuilder(req, resp, null);
        rb.setResults(context.queryDomainList);
        return rb;
    }

    // this is kind of awkward, but necessary since JsON faceting is protected in Solr,
    // (we can only interact with it by mocking up a request and parsing a resposne).
    private List<ResponseValue> parseResponse(SolrQueryResponse resp) {
        SimpleOrderedMap<Object> facet = (SimpleOrderedMap<Object>)((SimpleOrderedMap<Object>)resp.getValues()
                .get("facets")).get("fieldFacet");
        LinkedList<ResponseValue> values = new LinkedList<>();
        if(facet != null) {
            List<Object> buckets = (List<Object>) facet.get("buckets");

            for(int i =0; i <buckets.size(); ++i)
            {
                SimpleOrderedMap<Object> bucket = (SimpleOrderedMap<Object>)buckets.get(i);
                values.add(adapter.buildResponseValue(bucket));
            }
        }
        return values;
    }

    // see above
    public Map<String, Object> buildFacetJson()
    {
        int limit = 2*Math.max(requestNode.limit, 25);
        LinkedHashMap<String, Object> wrapper = new LinkedHashMap<>();
        LinkedHashMap<String, Object> facetName = new LinkedHashMap<>();
        LinkedHashMap<String, Object> type= new LinkedHashMap<>();
        LinkedHashMap<String, Object> facet = new LinkedHashMap<>();
        facet.put("field", adapter.field);
        facet.put("limit", limit);
        type.put("field", facet);
        facetName.put("fieldFacet", type);
        wrapper.put("facet", facetName);
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
