/**Copyright 2015-2016 CareerBuilder, LLC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
**/

package com.careerbuilder.search.relevancy.utility;

import com.careerbuilder.search.relevancy.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class ResponseUtility {

    // keeps all passed in values, up to request.limit
    // keeps as many generated values as possible up to request.limit
    public static ResponseValue[] filterAndSortValues(ResponseValue [] responses, RequestNode node, RelatednessRequest request) {
        List<ResponseValue> scoredValues = new ArrayList<>(Arrays.asList(responses));
        SortUtility.sortResponseValues(scoredValues, node.sort);


        if (request.return_popularity)
        {
            scoredValues = thresholdMinPop(scoredValues, request.min_popularity);
        }
        else
        {
            scoredValues = thresholdMinFGBGPop(scoredValues, request.min_popularity);
        }
        distinct(scoredValues);
        if(node.discover_values && scoredValues.size() > 0) {
            List<String> requestValues = distinct(node.values);
            int limit = Math.min(scoredValues.size(), node.limit);
            scoredValues = filterMergeResults(scoredValues, requestValues, limit, node.sort);
        }
        return scoredValues.toArray(new ResponseValue[scoredValues.size()]);
    }

    protected static List<ResponseValue> thresholdMinPop(List<ResponseValue> values, double threshold)
    {
        values = values.stream().filter((ResponseValue r) ->
                (r.popularity >= threshold)).collect(Collectors.toList());
        return thresholdMinFGBGPop(values, threshold);
    }

    protected static List<ResponseValue> thresholdMinFGBGPop(List<ResponseValue> values, double threshold) {
        return values.stream().filter((ResponseValue r) -> (r.background_popularity >= threshold
                        && r.foreground_popularity >= threshold)).collect(Collectors.toList());
    }

    protected static List<String> distinct(String [] requestValues )
    {
        return new LinkedList<>(
                (Arrays.stream(requestValues == null ? new String[0] : requestValues).collect(
                        Collectors.toMap(String::toLowerCase, s -> s, (first, second) -> first))
                        .values()));
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
    public static List<ResponseValue> filterMergeResults(List<ResponseValue> results,
                                                         List<String> requestValues, int limit, SortType sort)
    {
        List<String> keepSet = requestValues == null ? new LinkedList<>() : requestValues;
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
