#Semantic Knowledge Graph
*A graph structure, build automatically from a corpus of data, for traversing and measuring relationships within a domain*

The Semantic Knowledge Graph serves as a data scientist's toolkit, allowing you to discover and compare any entities modelled within a corpus of data from any domain. For example, if you indexed a corpus of job postings, you could figure out what the most related job titles are for the query "account manager", and subsequently what the top skills are for each of those job titles. If you were searching for restaurants and had a list keywords you want to rank based upon how similar they are to a combination of three different skills ? The Semantic Knowledge Graph will allow you to slice and dice the universe of terms and entites represented within your corpus in order to discover as many of these insights as you have the time and curiosity to pursue.

The Semantic Knowledge Graph is packaged as a request handler plugin for the popular Apache Solr search engine. Fundamentally, you must create a schema representing your corpus of data (from any domain), send the corpus of documents to Solr (script to do this is included), and then you can send queries to the Semantic Knowledge Graph request handler to discover and/or score relationships.

#Examples (from the job search domain):

...

