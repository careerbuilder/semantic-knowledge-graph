package com.careerbuilder.search.relevancy.scoring;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RelatednessStrategyTest {

    @Before
    public void init()
    {
    }

    @Test
    public void z_mean()
    {
        double expected = 0;
        double actual = RelatednessStrategy.z(50, 100, 25, 50);

        Assert.assertEquals(expected, actual, 1e-4);
    }
}
