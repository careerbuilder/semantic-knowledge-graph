package com.careerbuilder.search.relevancy.Runnable;

import com.careerbuilder.search.relevancy.Models.RequestNode;
import com.careerbuilder.search.relevancy.Models.ResponseValue;
import com.careerbuilder.search.relevancy.NodeContext;
import com.careerbuilder.search.relevancy.Runnable.Waitable;
import com.google.gson.Gson;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.facet.FacetModule;

import java.io.IOException;
import java.util.*;

public class FacetRunner extends Waitable{

    final NodeContext context;
    RequestNode requestNode;
    public List<ResponseValue> responses;
    String field;
    Gson gson;

    public FacetRunner(NodeContext context, RequestNode requestNode, String field) {
        this.gson = new Gson();
        this.requestNode = requestNode;
        this.context = context;
        this.field = field;
    }

    public void run()
    {
        try {
            facet();
        } catch (Exception e) { this.e = e; }
        notifyCallers();
    }

    public void facet() throws IOException {
        if (requestNode.values == null || requestNode.values.length == 0) {
            FacetModule mod = new FacetModule();
            SolrQueryResponse resp = new SolrQueryResponse();
            ResponseBuilder rb = getResponseBuilder(resp);
            mod.prepare(rb);
            mod.process(rb);
            responses = parseResponse(resp);
        }
    }

    private ResponseBuilder getResponseBuilder(SolrQueryResponse resp) throws IOException {
        SolrQueryRequest req =  new LocalSolrQueryRequest(context.req.getCore(), buildFacetParams());
        req.setJSON(buildFacetJson());
        ResponseBuilder rb = new ResponseBuilder(req, resp, null);
        rb.setResults(context.queryDomainList);
        return rb;
    }

    private List<ResponseValue> parseResponse(SolrQueryResponse resp) {
        SimpleOrderedMap<Object> facet = (SimpleOrderedMap<Object>)((SimpleOrderedMap<Object>)resp.getValues()
                .get("facets")).get("fieldFacet");
        List<Object> buckets = (List<Object>)facet.get("buckets");

        LinkedList<ResponseValue> values = new LinkedList<>();
        for(int i =0; i <buckets.size(); ++i)
        {
            SimpleOrderedMap<Object> bucket = (SimpleOrderedMap<Object>)buckets.get(i);
            values.add(new ResponseValue((String) bucket.get("val"), Double.valueOf((Integer)bucket.get("count"))));
        }
        return values;
    }

    public Map<String, Object> buildFacetJson()
    {
        int limit = 10 + 2*requestNode.limit;
        LinkedHashMap<String, Object> wrapper = new LinkedHashMap<>();
        LinkedHashMap<String, Object> facetName = new LinkedHashMap<>();
        LinkedHashMap<String, Object> type= new LinkedHashMap<>();
        LinkedHashMap<String, Object> facet = new LinkedHashMap<>();
        facet.put("field", field);
        facet.put("limit", limit);
        type.put("field", facet);
        facetName.put("fieldFacet", type);
        wrapper.put("facet", facetName);
        return wrapper;
    }

    public SolrParams buildFacetParams()
    {
        LinkedHashMap<String, String> paramMap = new LinkedHashMap<>();
        paramMap.put("facet.version", "1");
        paramMap.put("wt", "json");
        paramMap.put(FacetParams.FACET, "true");
        return new MapSolrParams(paramMap);
    }

}
