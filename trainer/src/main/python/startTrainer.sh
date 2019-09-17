#!/bin/sh
set -ex

#check python version
python -V
#run the python code
cd ./trainer/src/main/python
poetry run python ./run.py --model ../../../../$1 --data  ../../../../$2 --evaluator $3 --UUID  $4

qmake --version