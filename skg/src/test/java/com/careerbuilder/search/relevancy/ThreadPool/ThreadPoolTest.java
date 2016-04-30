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

package com.careerbuilder.search.relevancy.threadpool;

import com.careerbuilder.search.relevancy.runnable.Waitable;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

@RunWith(JMockit.class)
public class ThreadPoolTest {

    public class Dummy extends Waitable implements Callable<Waitable>{
        public int result;
        public Waitable call()
        {
            result = 1;
            return this;
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
        List<Future<Waitable>> futures = ThreadPool.getInstance().multiplex(dummyRunners);
        ThreadPool.getInstance().demultiplex(futures);

        Assert.assertTrue(true);
        for(int i = 0; i < dummyRunners.length; ++i)
        {
            Assert.assertEquals(1, dummyRunners[i].result);
        }
    }
}
