#!/usr/bin/python
from trainer import Trainer 
import os
import numpy as np
import sys
from model_parser.keras_parser import load_json


sys.path.append(
    os.path.join(
        os.path.dirname(__file__), 
        "../../../../evaluator/src/main/python/"
    )
)
from custom_env import CustomEvaluator
sys.path.append(os.path.join(os.path.dirname(__file__)))


if __name__ == "__main__":
    print("Currently in %s" % os.getcwd())

    x = np.load('./mock_data/xbnc_n.npy')
    y = np.load('./mock_data/ybnc_n.npy')

    evaluator = CustomEvaluator
    
    trainer = Trainer(
        { "x": x, "y": y },
        {
            "type" : "classification",
            "environment": evaluator
        })

    # trainer.train(input())
    trainer.train("./test.json")
