package com.careerbuilder.search.relevancy.utility;

import com.careerbuilder.search.relevancy.Models.ResponseValue;
import com.careerbuilder.search.relevancy.Models.SortType;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortUtility
{
    public static void sortResponseValues(List<ResponseValue> responseValues, SortType sortedBy)
    {

        if(sortedBy == null)
            sortedBy = SortType.relatedness;
        switch(sortedBy) {
            case foreground_popularity:
                sortByFG(responseValues);
                break;
            case popularity:
                sortByPopularity(responseValues);
                break;
            case background_popularity:
                sortByBG(responseValues);
                break;
            case relatedness:
                sortByRelatedness(responseValues);
                break;
        }
    }

    private static void sortByRelatedness(List<ResponseValue> values)
    {
        Collections.sort(values, Comparator.comparing((ResponseValue val) -> -1 * val.relatedness).thenComparing((ResponseValue val) -> val.value.toLowerCase()));
    }

    private static void sortByFG(List<ResponseValue> values)
    {
        Collections.sort(values, Comparator.comparing((ResponseValue val) -> -1 * val.foreground_popularity).thenComparing((ResponseValue val) -> val.value.toLowerCase()));
    }

    private static void sortByBG(List<ResponseValue> values)
    {
        Collections.sort(values, Comparator.comparing((ResponseValue val) -> -1 * val.background_popularity).thenComparing((ResponseValue val) -> val.value.toLowerCase()));
    }

    private static void sortByPopularity(List<ResponseValue> values)
    {
        Collections.sort(values, Comparator.comparing((ResponseValue val) -> -1 * val.popularity).thenComparing((ResponseValue val) -> val.value.toLowerCase()));
    }
}
