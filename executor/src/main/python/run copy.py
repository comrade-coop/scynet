from executor import Executor
import numpy as np
import sys
import os

sys.path.append(
    os.path.join(
        os.path.dirname(__file__),
        "../../../../evaluator/src/main/python/"
    )
)

from dummy_env import DummyEvaluator
sys.path.append(os.path.join(os.path.dirname(__file__)))

sys.path.append(
    os.path.join(
        os.path.dirname(__file__),
        "../../../../trainer/src/main/python/"
    )
)

from trainer import Trainer
sys.path.append(os.path.join(os.path.dirname(__file__)))


if __name__ == "__main__":

    dummy_evaluator = DummyEvaluator

    x = np.load('./mock_data/xbnc_n.npy')
    y = np.load('./mock_data/ybnc_n.npy')

    data = {
        "x": x,
        "y": y
    }

    config = {
        "type" : "classification",
        "is_executor": True,
        "environment": dummy_evaluator
    }

    executor = Trainer(data, config)
    # TODO This should restore an already trained model
    executor.train("./test.json", epochs=1, std=False)
    # trainer.train(input())
    print(executor.predict())