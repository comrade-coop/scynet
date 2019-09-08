#!/bin/sh
set -ex
echo "Hello"

#setting up env
eval "$(pyenv init -)"
eval "$(pyenv virtualenv-init -)"
pyenv activate trainer

#check python version
python -V
#run the python code
python ./trainer/src/main/python/run.py --model $1 --data_x $2 --data_y $3 --evaluator $4 --UUID $5

