package com.careerbuilder.search.relevancy.utility;

import com.careerbuilder.search.relevancy.Models.RequestNode;
import com.careerbuilder.search.relevancy.Models.ResponseNode;
import com.careerbuilder.search.relevancy.Models.ResponseValue;
import com.careerbuilder.search.relevancy.Models.SortType;
import com.careerbuilder.search.relevancy.utility.SortUtility;

import java.util.*;
import java.util.stream.Collectors;

public class ResponseUtility {

    // keeps all passed in values, up to request.limit
    // keeps as many generated values as possible up to request.limit
    public static void filterAndSortValues(ResponseNode response, RequestNode request, double minPopularity) {
        int limit = Math.min(response.values.length, request.limit);
        List<ResponseValue> responseValues = new ArrayList<>(Arrays.asList(response.values));
        SortUtility.sortResponseValues(responseValues, request.sort);
        responseValues = thresholdMinCount(responseValues, minPopularity);
        distinct(responseValues);
        if(request.discover_values && responseValues.size() > 0) {
            limit = Math.min(responseValues.size(), request.limit);
            responseValues = filterMergeResults(responseValues, request.values, limit, request.sort);
        }
        ResponseValue[] shrunk = responseValues.toArray(new ResponseValue[responseValues.size()]);
        response.values = shrunk;
    }

    public static List<ResponseValue> thresholdMinCount(List<ResponseValue> values, double minPopularity) {
        values = values.stream().filter((ResponseValue r) -> r.popularity >= minPopularity
                && r.background_popularity >= minPopularity
                && r.foreground_popularity >= minPopularity).collect(Collectors.toList());
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

    protected static boolean expandedContains(List<String> strings, ResponseValue responseValue)
    {
        for(String str: strings)
        {
            ResponseValue listValue = new ResponseValue(str);
            if(responseValue.valuesEqual(listValue))
                return true;
        }
        return false;
    }
}
