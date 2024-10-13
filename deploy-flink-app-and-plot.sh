#!/bin/bash

source ./deploy-flink-app.sh

if [ -z "$DESCRIPTION" ]; then
  python3 misc/plotter/results_plotter.py
else
  python3 misc/plotter/results_plotter.py --description="$DESCRIPTION"
fi

rm -rf "${RESULTS_DIRECTORY}/${EXPERIMENT_ID}"