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
import com.careerbuilder.search.relevancy.NodeContext;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.TotalHitCountCollector;

import java.io.IOException;

public class ScoreNormalizer {

    public static void normalize(NodeContext context, ResponseValue[] values) {
        int totalDocs = 1;
        try {
           totalDocs = getTotalDocs(context);
        } catch (IOException e) {}

        normalizeValues(totalDocs, values);
    }

    private static int getTotalDocs(NodeContext context) throws IOException {
        TotalHitCountCollector collector = new TotalHitCountCollector();
        context.req.getSearcher().search(new MatchAllDocsQuery(), collector);
        return collector.getTotalHits();
    }

    private static void normalizeValues(int totalDocs, ResponseValue [] values) {
        for(int i = 0; i < values.length; ++i) {
            values[i].popularity = normalizeFunc(totalDocs, values[i].popularity);
            values[i].foreground_popularity = normalizeFunc(totalDocs, values[i].foreground_popularity);
            values[i].background_popularity = normalizeFunc(totalDocs, values[i].background_popularity);
        }
    }

    private static double normalizeFunc(int total, double value) {
        return Math.round((value *1e6) / total);
    }
}
