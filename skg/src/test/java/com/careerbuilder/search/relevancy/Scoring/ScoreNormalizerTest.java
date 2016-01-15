package com.careerbuilder.search.relevancy.scoring;

import com.careerbuilder.search.relevancy.model.ResponseValue;
import mockit.Deencapsulation;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.TestCase.assertEquals;

@RunWith(JMockit.class)
public class ScoreNormalizerTest {

    @Test
    public void normalizeValues()
    {
        ResponseValue one = new ResponseValue("test");
        one.popularity = 1;
        one.foreground_popularity=1;
        one.background_popularity=1;

        ResponseValue two = new ResponseValue("test");
        two.popularity=2;
        two.foreground_popularity=2;
        two.background_popularity=2;

        ResponseValue [] values = new ResponseValue[] { one, two };

        Deencapsulation.invoke(ScoreNormalizer.class, "normalizeValues", (int)10, values);

        Assert.assertEquals(100000, values[0].popularity, 1e-4);
        Assert.assertEquals(100000, values[0].background_popularity, 1e-4);
        Assert.assertEquals(100000, values[0].foreground_popularity, 1e-4);
        Assert.assertEquals(200000, values[1].popularity, 1e-4);
        Assert.assertEquals(200000, values[1].background_popularity, 1e-4);
        Assert.assertEquals(200000, values[1].foreground_popularity, 1e-4);

    }

}
