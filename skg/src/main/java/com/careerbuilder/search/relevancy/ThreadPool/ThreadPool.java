package com.careerbuilder.search.relevancy.ThreadPool;

import com.careerbuilder.search.relevancy.Runnable.Waitable;
import org.apache.solr.common.SolrException;

import java.util.LinkedList;

public class ThreadPool
{
    private final PoolWorker[] threads;
    private final LinkedList<Runnable> queue;
    private static ThreadPool instance;

    public static synchronized ThreadPool getInstance() {
        instance = instance == null ? new ThreadPool() : instance;
        return instance;
    }

    public ThreadPool()
    {
        queue = new LinkedList<Runnable>();
        int nThreads = Math.min(Runtime.getRuntime().availableProcessors(), 1);
        threads = new PoolWorker[nThreads];

        for (int i=0; i<nThreads; i++) {
            threads[i] = new PoolWorker();
            threads[i].start();
        }
    }

    public void execute(Runnable r) {
        synchronized(queue) {
            queue.addLast(r);
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

    public static void multiplex(Runnable [] array)
    {
        for(int i = 0; i < array.length; ++i) {
            if(array[i] != null) {
                getInstance().execute(array[i]);
            }
        }
    }

    private class PoolWorker extends Thread {
        public void run() {
            Waitable r;
            while (true) {
                synchronized(queue) {
                    while (queue.isEmpty()) {
                        try {
                            queue.wait();
                        }
                        catch (InterruptedException ignored) {}
                    }
                    r = (Waitable) queue.removeFirst();
                }
                try {
                    r.run();
                }
                catch (Exception e) {
                   r.e = e;
                }
            }
        }
    }
}