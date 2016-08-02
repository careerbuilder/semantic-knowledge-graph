package com.careerbuilder.search.relevancy.model;

public class KnowledgeGraphRequest
{

    private static final int DEFAULT_MIN_POPULARITY = 1;

    public KnowledgeGraphRequest() {
        normalize = true;
        min_popularity = DEFAULT_MIN_POPULARITY;
        return_popularity = true;
    }

    public KnowledgeGraphRequest(RequestNode[] compare)
    {
        this.compare =  compare;
    }

    public String [] queries;
    public String [] foreground_queries;
    public String [] background_queries;
    public double min_popularity;
    public boolean return_popularity;
    public boolean normalize;
    public RequestNode[] compare;
}
