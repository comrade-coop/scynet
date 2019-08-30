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

class Executor:
    def __init__(self, data, trainer_config=None):

        if trainer_config is None:
            trainer_config = {
                'type': 'classification',
                'environment': None
            }
        self.data = data
        self.trainer = Trainer(data, trainer_config)
    
    def predict(self):
        return self.trainer.predict(self.data)