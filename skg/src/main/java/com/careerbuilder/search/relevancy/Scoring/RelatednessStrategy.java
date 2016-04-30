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

public class RelatednessStrategy {

    public static double z(int fgTotal, int bgTotal, double fgCount, double bgCount) {
        double bgProb = (bgCount / bgTotal);
        double num = fgCount - fgTotal * bgProb;
        double denom = Math.sqrt(fgTotal * bgProb * (1 - bgProb));
        denom = (denom == 0) ? 1e-10 : denom;
        double z = num / denom;
        double result = 0.2*sigmoid(z, -80, 50)
                + 0.2*sigmoid(z, -30, 30)
                + 0.2*sigmoid(z, 0, 30)
                + 0.2*sigmoid(z, 30, 30)
                + 0.2*sigmoid(z, 80, 50);
        return Math.round(result * 1e5) / 1e5;
    }

    private static double sigmoid(double x, double offset, double scale) {
        return (x+offset) / (scale + Math.abs(x+offset));
    }
}

