#!/usr/bin/python

from executor import Executor
import numpy as np
import sys
import os

print("Initializing Executor Python Client")

sys.path.append(
    os.path.join(
        os.path.dirname(__file__),
        "../../../../trainer/src/main/python/"
    )
)

from trainer import Trainer

sys.path.append(os.path.join(os.path.dirname(__file__)))
    
sys.path.append(
    os.path.join(
        os.path.dirname(__file__),
        "../../../../evaluator/src/main/python/"
    )
)

from dummy_env import WrapMeanEvaluator

sys.path.append(os.path.join(os.path.dirname(__file__)))

from model_parser.keras_parser import load_json
import argparse
from pyignite import Client, GenericObjectMeta
from pyignite.datatypes import String
from collections import OrderedDict

# Construct the argument parser
ap = argparse.ArgumentParser()

ap.add_argument("-o", "--output_cache_name", required=True,
   help="The name of the ignite cache where this is going to store results")

ap.add_argument("-c", "--cache_name", required=True,
   help="Cache where this script can find it's data and weights for example")

ap.add_argument("-id", "--UUID", required=True,
   help="Specify Job UUID")

args = vars(ap.parse_args())

if __name__ == "__main__":

    dummy_evaluator = WrapMeanEvaluator # Dummy

    client = Client()
    client.connect('127.0.0.1', 10800)

#     output_cache = client.get_or_create_cache(args['output_cache_name'])
    print(args)
    cache = client.get_cache(args['cache_name'])
    result = cache.get(args["UUID"])

    for k, v in result:
        print(k,v)

#     client.close()
#
#     x = np.load(args['data_x'])
#
#     data = {
#         "x": x,
#         "y": None
#     }
#
#     config = {
#         "type" : "classification",
#         "is_executor": True,
#         "split_strategy": None,
#         "environment": dummy_evaluator
#     }
#
#     executor = Trainer(data, config)
#     executor.restore_model(args['weights'], args['model'])
#     print(executor.predict(x))