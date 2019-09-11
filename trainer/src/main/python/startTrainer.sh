#!/bin/sh
set -ex

#check python version
python -V
#run the python code
cd ./trainer/src/main/python
poetry run python ./run.py --model ../../../../$1 --data_x  ../../../../$2 --data_y  ../../../../$3 --evaluator $4 --UUID  $5

