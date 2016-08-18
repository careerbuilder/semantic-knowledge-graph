#Semantic Knowledge Graph
*A graph structure, build automatically from a corpus of data, for traversing and measuring relationships within a domain*

The Semantic Knowledge Graph serves as a data scientist's toolkit, allowing you to discover and compare any entities modelled within a corpus of data from any domain. For example, if you indexed a corpus of job postings, you could figure out what the most related job titles are for the query "account manager", and subsequently what the top skills are for each of those job titles. If you were searching for restaurants and had a list keywords you want to rank based upon how similar they are to a combination of three different skills ? The Semantic Knowledge Graph will allow you to slice and dice the universe of terms and entites represented within your corpus in order to discover as many of these insights as you have the time and curiosity to pursue.

The Semantic Knowledge Graph is packaged as a request handler plugin for the popular Apache Solr search engine. Fundamentally, you must create a schema representing your corpus of data (from any domain), send the corpus of documents to Solr (script to do this is included), and then you can send queries to the Semantic Knowledge Graph request handler to discover and/or score relationships.

#Examples (from the job search domain):

*Request:*
```
curl -X POST http://localhost:8983/solr/skg/rel \
-H "Content-Type: application/json" \
-d \
'{
  "queries": [
    "keywords:\"data scientist\""
  ],
  "compare": [
    {
      "type": "jobtitle",
      "limit": 1,
      "compare": [
        {
          "type": "skills",
          "limit": 5,
          "discover_values": true,
          "values": [
            "java (programming language)"
          ]
        }
      ]
    }
  ]
}'
```

Response:
```
{ "data": [
  {
    "type": "jobtitle",
    "values": [
      {
        "id": "",
        "name": "Data Scientist",
        "relatedness": 0.989,
        "popularity": 86.0,
        "foreground_popularity": 86.0,
        "background_popularity": 142.0,
        "compare": [
          {
            "type": "skills.v3",
            "values": [
              {
                "id": "",
                "name": "Machine Learning",
                "relatedness": 0.97286,
                "popularity": 54.0,
                "foreground_popularity": 54.0,
                "background_popularity": 356.0
              },
              {
                "id": "",
                "name": "Predictive Modelling",
                "relatedness": 0.94565,
                "popularity": 27.0,
                "foreground_popularity": 27.0,
                "background_popularity": 384.0
              },
              {
                "id": "",
                "name": "Artificial Neural Networks",
                "relatedness": 0.94416,
                "popularity": 10.0,
                "foreground_popularity": 10.0,
                "background_popularity": 57.0
              },
              {
                "id": "",
                "name": "Apache Hadoop",
                "relatedness": 0.94274,
                "popularity": 50.0,
                "foreground_popularity": 50.0,
                "background_popularity": 1418.0
              },
              {
                "id": "",
                "name": "Java (Programming Language)",
                "relatedness": 0.76606,
                "popularity": 37.0,
                "foreground_popularity": 37.0,
                "background_popularity": 17442.0
              }
            ]
          }
        ]
      }
    ]
  }
 ]
}
```

#Available Request Parameters
**queries**	String[]  
A set of Solr queries which will be used to generate entities for scoring. If no foreground queries are supplied, these queries will also be used to score the entities. Multiple queries are merged to find the intersection between them (equivalent of a boolean AND query). See the types parameter for query field types. 

Note: the default operator between keywords is OR. If you wish to search for multiple words as a phrase, wrap them in quotes. If you want to make all keywords required, add a + before them or insert an AND between them: 
    senior java developer = senior OR java OR developer 
    +senior +java +developer = senior AND java AND developer 
    "senior java developer" = Exact phrase match for all three words in order 
    "senior" "java developer" = senior OR "java developer"

**compare**	Object[]  
An arbitrarily nested (recursive) list of objects corresponding to entity types to generate and score. Each item in the comparison list will generate scored lists of values based upon the requested relationship to their containing parent.

**type**	String  
The type of entity to generate or score. These types correspond to the fields in your Solr schema.xml 

**sort** optional	String  
The field to sort on. Supported fields include: 
  relatedness (statistical correlation)
  popularity (count per 1 million documents)
  foreground_popularity (popularity within the foreground query)
  background_popularity (popularity within the background query)

Defaults to relatedness.

**limit** optional	integer  
The limit on the result set size. Defaults to 1.

**values** optional	String[]  
A set of values to score. Only exact id or name matches are will be scored correctly. Note: unless passed-in values do not meet the minimum popularity requirement, passed-in values override generated values in the result set. For example, if three values are passed in and a limit of ten is set, the top seven generated values will be returned along with the three passed-in values, regardless of scores or popularity of the passed-in values.

**discover_values** optional boolean  
Whether or not to generate values. If set to true, the query in the queries parameter will be used to automatically generate a set of values to score. Defaults to "true" if no values are passed in in the values parameter, "false" otherwise.

**compare** optional	Object[]  
A list of nested request objects. For each value returned for this entity type, all nested request object entity types will be generated and scored. Note: the "queries" and "foreground_queries" parameters are combined with the parent entity value when generating / scoring nested entities.

**foreground_queries** optional	String[]  
If supplied, a set of Solr queries used to score the results generated using the queries in the queries parameter. The relatedness score will measure statistical skewedness toward the foreground_queries queries merged together using AND. Defaults to the value of the queries parameter. See the types parameter for query field types.

**background_queries** optional	String[]  
If supplied, a set of Solr queries used to score the results generated using the queries in the queries parameter. The relatedness score will measure statistical skewedness away from the background_queries queries merged together using AND. Defaults to match all documents. See the types parameter for query field types.

**min_popularity** optional	double  
The minimum popularity of returned results (assuming exactly 1 million total documents). Results which have a popularity, foreground popularity, or background popularity lower than min_popularity out of 1 million are omitted. Defaults to at least 1 hit per million documents.

**normalize_values** optional	boolean  
Whether the API should attempt to find ids and names for passed-in values. If false, the API will return passed-in values in the name field without regard to whether they represent an id or name for an entity. Turning normalization off may boost performance. Defaults to "true."

#Building and Running
The easiest way to build the Semantic Knowledg Graph is to run the `build.sh` script in the root directory of the project (or `rebuild.sh`, which will build and launch an Apache Solr instance with the Semantic Knowledge Graph configured). The final application will be found in the `deploy` directory, and you can launch it using the `restart-solr.sh` script found in that directory. You can simply copy this `deploy` folder to your production environment and run the `restart-solr.sh` script to launch the service. By default, you can hit it at `http://localhost:8983/solr/knowledge-graph/rel`. See the example in the Examples section above for usage.

#Using the System
Once the Semantic Knowledge Graph project has been built, you need to indexing a corpus of data through it by running the `feed.sh` script. The fields you include in your corpus should correspond to the fields defined in your Solr `schema.xml` found in the `deploy/solr/knowledge-graph/conf` directory.
