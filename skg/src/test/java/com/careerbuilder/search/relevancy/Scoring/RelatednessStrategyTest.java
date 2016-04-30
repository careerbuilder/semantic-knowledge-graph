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
