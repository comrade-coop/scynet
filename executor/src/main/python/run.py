from executor import Executor
import numpy as np
import sys
import os

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


# TODO THINK ABOUT COMMUNICATION
# TODO Refactor executor
# TODO Think all of this through
if __name__ == "__main__":

    dummy_evaluator = WrapMeanEvaluator # Dummy

    x = np.load('./mock_data/xbnc_n.npy')

    data = {
        "x": x,
        "y": None
    }

    config = {
        "type" : "classification",
        "is_executor": True,
        "split_strategy": None,
        "environment": dummy_evaluator
    }

    executor = Trainer(data, config)
    executor.restore_model("./testmodel.h5", "./test.json")
    print(executor.predict(x))