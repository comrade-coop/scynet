#!/bin/sh
set -ex

#check python version
python -V
#run the python code
uuid="3f5e19bf-cec8-4ab3-9acb-83f13997356c"
a="mock/temp/results/${uuid}_w.h5"
b="mock/temp/data/xbnc_n_TEMP${uuid}.npy"
c="basic"

# cd ./trainer/src/main/python
poetry run python ./run.py --predict --model-path ../kotlin/$a --data_x  ../kotlin/$b --evaluator $c --UUID  $uuid

qmake --version