package com.careerbuilder.search.relevancy.ThreadPool;

import com.careerbuilder.search.relevancy.Runnable.Waitable;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(JMockit.class)
public class ThreadPoolTest {

    public class Dummy extends Waitable implements Runnable{
        public int result;
        public void run()
        {
            result = 1;
            notifyCallers();
        }
    }
    Dummy [] dummyRunners = new Dummy[4];

    @Before
    public void init() {
        for(int i =0; i < dummyRunners.length; ++i)
        {
            dummyRunners[i] = new Dummy();
        }
    }


    @Test(timeout=10000)
    public void multiplex_demultiplex() throws IOException
    {
        ThreadPool.getInstance().multiplex(dummyRunners);
        ThreadPool.getInstance().demultiplex(dummyRunners);

        Assert.assertTrue(true);
        for(int i = 0; i < dummyRunners.length; ++i)
        {
            Assert.assertEquals(1, dummyRunners[i].result);
        }
    }
}
