package com.careerbuilder.search.relevancy.ThreadPool;

import com.careerbuilder.search.relevancy.Runnable.Waitable;
import org.apache.solr.common.SolrException;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.WeakHashMap;

public class ThreadPool
{
    private final PoolWorker[] threads;
    private final LinkedList<Waitable> queue;
    private static Integer ids = 0;

    private static Map<Integer, ThreadPool> localPool = Collections.synchronizedMap(new WeakHashMap<>());

    private static final ThreadLocal<Integer> id = new ThreadLocal<Integer>() {
        @Override protected Integer initialValue() {
            ids = ids % Integer.MAX_VALUE;
            return ids++;
        }
    };



    public static ThreadPool getInstance() {
        ThreadPool pool = localPool.get(id.get());
        if(pool == null)
        {
            pool = new ThreadPool();
            localPool.put(id.get(), pool);
        }
        return pool;
    }

    public ThreadPool()
    {
        queue = new LinkedList<>();
        int nThreads =40;
        threads = new PoolWorker[nThreads];

        for (int i=0; i<nThreads; i++) {
            threads[i] = new PoolWorker();
            threads[i].start();
        }
    }

    public void execute(Waitable w) {
        synchronized(queue) {
            queue.addLast(w);
            queue.notify();
        }
    }

    public static void demultiplex(Waitable[] array)
    {
        try {
            for(int i = 0; i < array.length; ++i) {
                if(array[i] != null) {
                    synchronized (array[i]) {
                        if (!array[i].done) {
                            array[i].wait();
                        }
                    }
                    if (array[i].e != null) {
                        throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,
                                array[i].e.getMessage(),
                                array[i].e);
                    }
                }
            }
        } catch (InterruptedException e) {
            throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,
                    "Parallel operation interrupted");
        }
    }

    public static void multiplex(Waitable [] array)
    {
        for(int i = 0; i < array.length; ++i) {
            if(array[i] != null) {
                getInstance().execute(array[i]);
            }
        }
    }

    private class PoolWorker extends Thread {
        public void run() {
            Waitable w;
            while (true) {
                synchronized(queue) {
                    while (queue.isEmpty()) {
                        try {
                            queue.wait();
                        }
                        catch (InterruptedException e) {
                            throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,
                                    "Parallel operation interrupted");
                        }
                    }
                    w = queue.removeFirst();
                }
                try {
                    w.run();
                }
                catch (Exception e) {
                   w.e = e;
                }
            }
        }
    }
}