package com.careerbuilder.search.relevancy.utility;

import com.careerbuilder.search.relevancy.Models.RelatednessRequest;
import com.careerbuilder.search.relevancy.Models.ResponseNode;
import com.careerbuilder.search.relevancy.Models.ResponseValue;
import com.careerbuilder.search.relevancy.Models.SortType;
import com.careerbuilder.search.relevancy.utility.ResponseUtility;
import com.careerbuilder.search.relevancy.utility.SortUtility;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

public class ResponseUtilityTest {

    private ResponseNode nodeWithDups;
    private List<ResponseValue> valuesDups;
    private List<ResponseValue> values;
    private ResponseNode node;
    private String [] requestValuesDups = new String [] { "v1", "v1", "v3", "V3", "V3", "v4"};

    @Before
    public void init()
    {
        nodeWithDups = new ResponseNode();
        node = new ResponseNode();
        ResponseValue v1 = new ResponseValue("v1", 0.75);
        ResponseValue v2 = new ResponseValue("v2", 0.5);
        ResponseValue v3 = new ResponseValue("v3", 0.25);
        ResponseValue v4 = new ResponseValue("v4", 0);
        v1.background_popularity=1;
        v1.popularity=1;
        v2.background_popularity=1;
        v2.popularity=0;
        v3.background_popularity=0;
        v3.popularity=2;
        v4.background_popularity=1;
        v4.popularity=1;
        valuesDups = new LinkedList<>();
        valuesDups.add(v4);
        valuesDups.add(v3);
        valuesDups.add(v2);
        valuesDups.add(v2);
        valuesDups.add(v2);
        valuesDups.add(v1);
        valuesDups.add(v1);
        values = new LinkedList<>();
        values.add(v1);
        values.add(v2);
        values.add(v3);
        values.add(v4);
    }

    @Test
    public void distinct_Request()
    {
        String [] expected = new String[] {"v1", "v3", "v4"};
        List<String> actual = ResponseUtility.distinct(requestValuesDups);

        Assert.assertArrayEquals(expected, actual.toArray(new String[0]));
    }

    @Test
    public void distinct()
    {
        SortUtility.sortResponseValues(valuesDups, SortType.foreground_popularity);
        ResponseUtility.distinct(valuesDups);

        assertEquals(4, valuesDups.size());
        assertEquals("v1", valuesDups.get(0).value);
        assertEquals("v2", valuesDups.get(1).value);
        assertEquals("v3", valuesDups.get(2).value);
        assertEquals("v4", valuesDups.get(3).value);
    }

    @Test
    public void filterMergeKeep()
    {
        List<String> keep = Arrays.asList(new String [] {"v4"});

        List<ResponseValue> actual = ResponseUtility.filterMergeResults(values, keep, 2, SortType.foreground_popularity);

        assertEquals(2, actual.size());
        assertEquals("v1", actual.get(0).value);
        assertEquals("v4", actual.get(1).value);
    }

    @Test
    public void filterMerge_noKeep()
    {
        List<String> keep = new LinkedList<>();

        List<ResponseValue> actual = ResponseUtility.filterMergeResults(values, keep, 2, SortType.foreground_popularity);

        assertEquals(2, actual.size());
        assertEquals("v1", actual.get(0).value);
        assertEquals("v2", actual.get(1).value);
    }

    @Test
    public void filterMergeOnlyKeep()
    {
        List<String> keep = Arrays.asList(new String [] { "v3", "v4", "v2"});

        List<ResponseValue> actual = ResponseUtility.filterMergeResults(values, keep, 2, SortType.foreground_popularity);

        assertEquals(3, actual.size());
        assertEquals("v2", actual.get(0).value);
        assertEquals("v3", actual.get(1).value);
        assertEquals("v4", actual.get(2).value);
    }

    @Test(expected= IllegalArgumentException.class)
    public void filterMergeException()
    {
        List<String> keep = Arrays.asList(new String[]{"v3", "v4", "v2", "v2", "v2"});

        List<ResponseValue> actual = ResponseUtility.filterMergeResults(values, keep, 2, SortType.foreground_popularity);
    }

    public void thresholdMinPopularityNoFilter()
    {
        List<ResponseValue> actual = ResponseUtility.thresholdMinPop(values, 0);

        assertEquals(4, actual.size());
        assertEquals("v1", actual.get(0).value);
        assertEquals("v2", actual.get(1).value);
        assertEquals("v3", actual.get(2).value);
        assertEquals("v4", actual.get(3).value);
    }

    public void thresholdMinPopularityFilter()
    {
        List<ResponseValue> actual = ResponseUtility.thresholdMinPop(values, 1);

        assertEquals(1, actual.size());
        assertEquals("v1", actual.get(0).value);
    }

    public void thresholdMinPopularityFilterNoReturn()
    {
        List<ResponseValue> actual = ResponseUtility.thresholdMinFGBGPop(values, 1);

        assertEquals(1, actual.size());
        assertEquals("v1", actual.get(0).value);
        assertEquals("v2", actual.get(1).value);
    }
}
