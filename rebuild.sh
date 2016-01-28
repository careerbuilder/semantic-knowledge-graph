cd skg/
mvn clean
mvn package
cd ../
ant package-nobuild
cd deploy
chmod +x restart-solr.sh
chmod +x restart-solr-dbg.sh
chmod +x solr/bin/solr
./restart-solr-dbg.sh

