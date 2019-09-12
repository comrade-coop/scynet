#!/bin/sh
set -ex
# print python version
python -V
#run the python code
python ./executor/src/main/python/run.py --output_cache_name "$1" --cache_name "$2" --UUID "$3"

