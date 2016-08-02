package com.careerbuilder.search.relevancy.model;

import com.careerbuilder.search.relevancy.utility.MapUtility;
import org.apache.solr.common.util.SimpleOrderedMap;

public class ResponseValue {
    public String value;
    public SimpleOrderedMap<String> normalizedValue;
    public double relatedness;
    public double foreground_popularity;
    public double background_popularity;
    public double popularity;
    public ResponseNode [] compare;

    public ResponseValue(String value) {
        this.value = value;
    }

    public ResponseValue(ResponseValue other)
    {
        value = other.value;
        normalizedValue = other.normalizedValue;
        relatedness = other.relatedness;
        foreground_popularity = other.foreground_popularity;
        background_popularity = other.background_popularity;
        popularity = other.popularity;
        compare = other.compare;
    }

    @Override
    public boolean equals(Object other)
    {
        if(other instanceof ResponseValue) {
            ResponseValue cast = (ResponseValue)other;
            return value.equals(cast.value) && relatedness == cast.relatedness && foreground_popularity == cast.foreground_popularity
                    && background_popularity == cast.background_popularity;
        }
        return false;
    }

    // nb: not transitive
    public boolean expandedEquals(ResponseValue other)
    {
        return this.valuesEqual(other)
                && foreground_popularity == other.foreground_popularity
                && background_popularity == other.background_popularity
                && popularity == other.popularity;
    }

    public boolean valuesEqual(ResponseValue other) {
        return this.value.equalsIgnoreCase(other.value)
                || MapUtility.mapContainsValue(this.value, other.normalizedValue)
                || MapUtility.mapContainsValue(other.value, this.normalizedValue)
                || MapUtility.mapsEqual(this.normalizedValue, other.normalizedValue);
    }

    public ResponseValue(String value, double foreground_popularity)
    {
        this(value);
        this.foreground_popularity = foreground_popularity;
    }

}
