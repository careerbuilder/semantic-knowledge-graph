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

package com.careerbuilder.search.relevancy.utility;

import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.solr.common.SolrException;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.SyntaxError;

import java.util.LinkedList;
import java.util.List;

public class ParseUtility
{
    public static List<Query> parseQueryStrings(String [] qStrings, SolrQueryRequest req) {
        if(qStrings != null) {
            LinkedList<Query> queryList = new LinkedList<Query>();
            for (String qString : qStrings) {
                queryList.add(parseQueryString(qString, req));
            }
            return queryList;
        }
        List<Query> deflt = new LinkedList<>();
        deflt.add(new MatchAllDocsQuery());
        return deflt;
    }

    public static Query parseQueryString(String qString, SolrQueryRequest req) {
        try {
            QParser parser = QParser.getParser(qString, req.getParams().get("defType"), req);
            return parser.getQuery();
        } catch (SyntaxError e) {
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,
                    "Syntax error in query: " + qString + ".");
        }
    }
}
