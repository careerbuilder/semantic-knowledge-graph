ant pull
cd lucene-solr
ant ivy-bootstrap
cd ../relatedness/
mvn clean
mvn package
cd ../
ant package
cd deploy
chmod +x restart-solrfst.sh
chmod +x restart-solrfst-dbg.sh
chmod +x feed.sh
chmod +x solrfst/bin/solr
