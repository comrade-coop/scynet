#!/usr/bin/python
from trainer import Trainer 
import os
import numpy as np
import sys
from model_parser.keras_parser import load_json
import argparse
from numpy import genfromtxt
# Construct the argument parser
ap = argparse.ArgumentParser()

# Add the arguments to the parser
ap.add_argument("-m", "--model", required=False,
   help="JSON Gattakka Model path")

ap.add_argument("-j", "--model_json", required=False,
   help="JSON Gattakka Model")

ap.add_argument("-dx", "--data_x", required=False,
   help="CSV Training Data path to be parsed to numpy array")

ap.add_argument("-dy", "--data_y", required=False,
   help="CSV GT Data path to be parsed to numpy array")

ap.add_argument("-e", "--evaluator", required=False,
   help="CSV GT Data path to be parsed to numpy array")

ap.add_argument("-id", "--UUID", required=False,
   help="Specify Job ID")

ap.add_argument("-pr", "--predict", required=False,
    help="Predict", action='store_true')

ap.add_argument("-mp", "--model-path", required=False,
    help="Model file path")

args = vars(ap.parse_args())

sys.path.append(
    os.path.join(
        os.path.dirname(__file__),
        "../../../../evaluator/src/main/python/"
    )
)

from custom_env import CustomEvaluator
sys.path.append(os.path.join(os.path.dirname(__file__)))


if __name__ == "__main__":



    if args['predict']:
        x = np.load(args['data_x'])

        evaluatorMap = {
            'basic': CustomEvaluator
        }

        evaluator = evaluatorMap[args['evaluator']]

        if evaluator is None:
            print("No evaluator found")

        data = None

        config = {
            "type" : "classification",
            "is_executor": False,
            "split_strategy": None,
            "environment": evaluator,
        }

        trainer = Trainer(data, config)

        trainer.restore_model(args['model_path'], None, from_deep_copy=True) # TODO use UUID

        res = trainer.predict([x])
        print("-" * 20 + ">   " + str(res) + "   <" + "-" * 20)

    else:

        x = np.load(args['data_x'])
        y = np.load(args['data_y'])

        evaluatorMap = {
            'basic': CustomEvaluator
            # Add more here
            # (TODO: Discuss semantics around evaluator/trainer/executor addressing)
        }

        evaluator = evaluatorMap[args['evaluator']]

        if evaluator is None:
            print("No evaluator found")

        data = {
            "x": x,
            "y": y
        }

        config = {
            "type" : "classification",
            "is_executor": False,
            "split_strategy": None,
            "environment": evaluator,
        }

        trainer = Trainer(data, config)

        # trainer.train(input())

        ### use this for 'executor mode'
        # trainer.restore_model("./trainer/src/main/python/testmodel.h5", "./trainer/src/main/python/test.json")

        #### use this for trainer mode

        stdInput = False

        if args["model"] is None and args["model-json"] is not None:
            stdInput = True

        trainer.train(args["model"], std=stdInput)
        trainer.save_model("../../../../trainer/src/main/kotlin/mock/temp/results/%s_w.h5" % (args["UUID"]), args["model"], deep_copy=True)

        # get performance
        # get weights and save them
        # Write to file and read from ignite
        print(trainer.predict(x))
        print("DONE=%s" % (trainer.val_loss)) # This is used as a signal to the ProcessBuilder to stop and gather stuff
        print("Saving results to ./trainer/src/main/kotlin/mock/temp/results/%s_w.h5" % (args["UUID"]))