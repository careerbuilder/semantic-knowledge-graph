ant pull
cd lucene-solr
ant ivy-bootstrap
cd ../skg/
mvn clean
mvn package
cd ../
ant package
cd deploy
chmod +x restart-solr.sh
chmod +x restart-solr-dbg.sh
chmod +x feed.sh
chmod +x solr/bin/solr
