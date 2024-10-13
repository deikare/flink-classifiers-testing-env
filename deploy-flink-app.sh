#!/bin/bash

if [ -z "$FLINK_BIN_PATH" ]; then
  echo "FLINK_BIN_PATH env variable not set" >&2
  exit 1
fi

if [ -z "$RESULTS_DIRECTORY" ]; then
  echo "RESULTS_DIRECTORY env variable not set" >&2
  exit 1
fi

if [ -z "$FLINK_ADDRESS" ]; then
  export FLINK_ADDRESS="localhost"
fi

if [ -z "$FLINK_PORT" ]; then
  export FLINK_PORT="8081"
fi

if [ "$FLINK_ADDRESS" = "localhost" ]; then ## if flink cluster is not running on cluster start it
  http_status=$(curl --write-out "%{http_code}" --silent --output /dev/null "http://${FLINK_ADDRESS}:${FLINK_PORT}/taskmanagers")

  if [ "${http_status}" != "200" ]; then
    "${FLINK_BIN_PATH}"/start-cluster.sh
  fi
fi

jar_dir="./target"
jar_path=$(ls -t $jar_dir/*.jar | head -n 1 )
if [ -z "$jar_path" ]; then
  echo "Error: No JAR file found in $jar_dir" >&2
  exit 1
fi

jar_absolute_path=$(realpath $jar_path)

export EXPERIMENT_ID=$(uuidgen)

"$FLINK_BIN_PATH"/flink run -m "${FLINK_ADDRESS}:${FLINK_PORT}" "$jar_absolute_path" --env EXPERIMENT_ID="$EXPERIMENT_ID" --env RESULTS_DIRECTORY="$RESULTS_DIRECTORY"