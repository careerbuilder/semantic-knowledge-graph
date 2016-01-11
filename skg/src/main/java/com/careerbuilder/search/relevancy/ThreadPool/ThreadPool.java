package com.careerbuilder.search.relevancy.ThreadPool;

import com.careerbuilder.search.relevancy.Runnable.Waitable;
import org.apache.solr.common.SolrException;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadPool
{
    private final ExecutorService pool = Executors.newCachedThreadPool();

    public static ThreadPool getInstance()
    {
        return ThreadPoolHolder.pool;
    }

    public static synchronized Future execute(Waitable w) {
        return getInstance().pool.submit(w);
    }

    public static List<Waitable> demultiplex(List<Future<Waitable>> futures)
    {
        List<Waitable> result = new LinkedList<Waitable>();
        try
        {
            for (Future<Waitable> future : futures)
            {
                result.add(future.get());
            }
        }
        catch (InterruptedException e)
        {
            throw new SolrException(
                    SolrException.ErrorCode.SERVER_ERROR, "Parallel Operation interrupted", e);
        }
        catch (ExecutionException e)
        {
            throw new SolrException(
                    SolrException.ErrorCode.SERVER_ERROR, "Execution exception. ", e);
        }
        return result;
    }

    public static List<Future<Waitable>> multiplex(Waitable [] array)
    {
        LinkedList<Future<Waitable>> futures = new LinkedList<>();
        for(int i = 0; i < array.length; ++i) {
            if(array[i] != null) {
                futures.addLast(execute(array[i]));
            }
        }
        return futures;
    }
}