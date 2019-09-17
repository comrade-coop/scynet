#!/bin/sh
set -ex

#check python version
python -V
#run the python code
uuid="6cf168a6-173f-4192-8a8e-469ea3398914"
a="mock/temp/results/${uuid}_w.h5"
b="mock/temp/data/xbnc_n_TEMP${uuid}.npy"
c="basic"

# cd ./trainer/src/main/python
poetry run python ./run.py --predict --model-path ../kotlin/$a --data_x  ../kotlin/$b --evaluator $c --UUID  $uuid

qmake --version