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
