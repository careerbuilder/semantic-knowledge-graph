cd relatedness/
mvn clean
mvn package
cd ../
cd solr-text-tagger
cd ../
ant package-nobuild
cd deploy
chmod +x restart-solrfst.sh
chmod +x restart-solrfst-dbg.sh
chmod +x solrfst/bin/solr
./restart-solrfst-dbg.sh

