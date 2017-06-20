Explanation for running solr_skg.py on Semantic Knowledge Graph

Cuno Duursma
cuno.duursma@cgi.com

Tested on Windows 7 Python 2.7 Solr 5.1.0

schema.xml shoud be copied to:
    semantic-knowledge-graph-master\deploy\solr\server\solr\knowledge-graph\conf\
    If you change schema.xml, make sure to remove documents and restart solr.
    
    Delete all (!) solr Knowlege Graph data (paste URL in browser):
        http://localhost:8983/solr/knowledge-graph/update?stream.body=<delete><query>*:*</query></delete> 
    Commit delete (paste URL in browser):
        http://localhost:8983/solr/knowledge-graph/update?stream.body=<commit/> 
    
    Restarting solr:
        Open command window:
            change to directory semantic-knowledge-graph-master\deploy\solr\server
            execute:
            java -DSTOP.PORT=7983 -DSTOP.KEY=solrrocks -jar start.jar --stop 
            Ports and Key may vary: see Solr console window in browser: http://localhost:8983/solr/#/ 
        Go to semantic-knowledge-graph-master\deploy
            source restart-solr.sh (e.g. from bash)
        Just doing the restart using restart-solr.sh did not work for me.


Running solr_skg.py sould produce:

Knowledge Graph feed result: <?xml version="1.0" encoding="UTF-8"?>
<response>
<lst name="responseHeader"><int name="status">0</int><int name="QTime">41</int></lst>
</response>

Knowledge Graph query:
{
  "min_popularity": 0.0, 
  "compare": [
    {
      "sort": "relatedness", 
      "limit": 5, 
      "type": "col1", 
      "discover_values": "true"
    }
  ], 
  "queries": [
    "col1:\"whale\""
  ]
}
Knowledge Graph results:
{
  "data": [
    {
      "values": [
        {
          "foreground_popularity": 400000.0, 
          "popularity": 400000.0, 
          "name": "whale", 
          "background_popularity": 400000.0, 
          "relatedness": 0.02618
        }, 
        {
          "foreground_popularity": 200000.0, 
          "popularity": 200000.0, 
          "name": "arctic", 
          "background_popularity": 200000.0, 
          "relatedness": 0.0163
        }, 
        {
          "foreground_popularity": 200000.0, 
          "popularity": 200000.0, 
          "name": "dolphin", 
          "background_popularity": 200000.0, 
          "relatedness": 0.0163
        }, 
        {
          "foreground_popularity": 100000.0, 
          "popularity": 100000.0, 
          "name": "sea", 
          "background_popularity": 100000.0, 
          "relatedness": 0.01097
        }
      ], 
      "type": "col1"
    }
  ]
}
      
Issues:

- Notice that the popularity results are not converted correctly from JSON (e.g. 400000.0 should be 4.0)
- Relatedness figures are very low for a z-score

