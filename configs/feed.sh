#!/bin/bash
CSVFOLDER=$1
CORENAME=$2

for file in $CSVFOLDER/*.csv
do
  curl http://localhost:8983/solr/$CORENAME/update?commit=true --data-binary @$file -H 'Content-type:text/csv'
done

