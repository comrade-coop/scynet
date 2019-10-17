#!/bin/sh
set -ex

#check python version
python -V
#run the python code
uuid=$1
a="mock/temp/results/${uuid}_w.h5"
dataset=$2
c="basic"

cd ./trainer/src/main/python
poetry run python ./run.py --evaluate --model-path ../kotlin/$a --data  ../../../../$dataset --evaluator $c --UUID  $uuid

qmake --version