#!/bin/bash

ESM_DATA_DIR=$1
if [ -z $ESM_DATA_DIR ] ; then
  ESM_DATA_DIR=./esm_data
fi

ESM_PORT=$2
if [ -z $ESM_PORT ] ; then
  ESM_PORT=8081
fi

java -jar appcore/target/esm.jar \
  --esm.db.choice=GraphDB \
  --graphdb.repository.id=pokemon \
  --graphdb.address=http://127.0.0.1:7277 \
  --server.port=${ESM_PORT} \
  --esm.db.data.dir="${ESM_DATA_DIR}"
