package com.careerbuilder.search.relevancy.utility;

import com.careerbuilder.search.relevancy.Models.*;
import com.careerbuilder.search.relevancy.utility.SortUtility;

import java.util.*;
import java.util.stream.Collectors;

public class ResponseUtility {

    // keeps all passed in values, up to request.limit
    // keeps as many generated values as possible up to request.limit
    public static void filterAndSortValues(ResponseNode response, RequestNode node, RelatednessRequest request) {
        int limit = Math.min(response.values.length, node.limit);
        List<ResponseValue> responseValues = new ArrayList<>(Arrays.asList(response.values));
        SortUtility.sortResponseValues(responseValues, node.sort);
        responseValues = thresholdMinPopularity(responseValues, request);
        distinct(responseValues);
        if(node.discover_values && responseValues.size() > 0) {
            limit = Math.min(responseValues.size(), node.limit);
            responseValues = filterMergeResults(responseValues, node.values, limit, node.sort);
        }
        ResponseValue[] shrunk = responseValues.toArray(new ResponseValue[responseValues.size()]);
        response.values = shrunk;
    }

    public static List<ResponseValue> thresholdMinPopularity(List<ResponseValue> values, RelatednessRequest request) {
        values = values.stream().filter((ResponseValue r) ->
                (r.popularity >= request.min_popularity || !request.return_popularity)
                && r.background_popularity >= request.min_popularity
                && r.foreground_popularity >= request.min_popularity).collect(Collectors.toList());
        return values;
    }

    public static void distinct(List<ResponseValue> responseValues)
    {
        for(int i = 1; i < responseValues.size();) {
            if(responseValues.get(i).expandedEquals(responseValues.get(i-1))) {
                responseValues.remove(i-1);
            }
            else { ++i; }
        }
    }

    // move any keep values (request values) beyond the request limit to just within the request limit, replacing generated values
    public static List<ResponseValue> filterMergeResults(List<ResponseValue> results, String[] requestValues, int limit, SortType sort)
    {
        List<String> keepSet = Arrays.asList(requestValues == null ? new String[0] : requestValues);
        if (keepSet.size() > results.size())
            throw new IllegalArgumentException("The keep set is larger than the results set.");

        List<ResponseValue> keepList= new LinkedList<>();
        List<ResponseValue> disposableList = new LinkedList<>();
        for (ResponseValue r : results)
        {
            if (keepSet.stream().anyMatch(s -> r.valuesEqual(new ResponseValue(s))))
                keepList.add(r);
            else
                disposableList.add(r);
        }

        if (keepList.size() < limit)
        {
            int numResultsToAdd = Math.min(limit - keepList.size(), disposableList.size());
            keepList.addAll(disposableList.subList(0, numResultsToAdd));
        }
        SortUtility.sortResponseValues(keepList, sort);
        return keepList;
    }
}
