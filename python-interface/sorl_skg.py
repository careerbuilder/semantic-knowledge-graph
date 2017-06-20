# -*- coding: utf-8 -*-
"""
Created on Tue Jun 13 15:08:17 2017

@author: cduursma
cuno.duursma@cgi.com 
Licenced under Apache License 2.0

Python interface to the Semantic Knowledge Graph in Solr
https://github.com/careerbuilder/semantic-knowledge-graph 
Tested on Windows 7 Python 2.7 Solr 5.1.0
            
"""

import requests, json

# Global Knowledge Graph Query settings
url_query = "http://localhost:8983/solr/knowledge-graph/rel"
url_update = "http://localhost:8983/solr/knowledge-graph/update"
headers_query = {"content-type": "application/json", "Accept-Charset": "UTF-8"}
headers_update = {'Content-type': 'text/csv',"Accept-Charset": "UTF-8"}
params_update = {"commit": "true"}
data_update = open("rr_total2.csv", "rb").read()

# Example finding "five" in "col1"
query_content = {"queries":["col1:\"whale\""],
         "min_popularity":0.0,
         "compare":[{"type":"col1", "limit":5, "sort":"relatedness", "discover_values": "true"}]}


def feed_skg(data_update):
    """Feeds the knowledge graph with data. Data must be a binary openened file matching Knowledeg Graph schema.xml"""
    rf = requests.get(url_update, params=params_update, headers=headers_update, data=data_update)
    return(rf)
        

def query_skg(query):
    """Queries the knowledge graph with query. Query must be a Python set representing JSON object"""
    rq = requests.post(url_query, headers=headers_query, json=query)
    return(rq)


if __name__ == '__main__':
    data_update = open("example.csv", "rb").read()
    rf=feed_skg(data_update)
    print("Knowledge Graph feed result: {0}".format(rf.text))
    parsed_query=json.loads(json.dumps(query_content, indent=2, sort_keys=False))
    print("Knowledge Graph query:")
    print(json.dumps(query_content, indent=2))
    rq=query_skg(query_content)
    parsed=json.loads(rq.text)
    print("Knowledge Graph results:")
    print(json.dumps(parsed, indent=2, sort_keys=False))
    
        
    
