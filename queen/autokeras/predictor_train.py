import pickle
import argparse
import os

from keras.datasets import cifar100 as dataset
from autokeras import ImageClassifier
from autokeras.search import Searcher
from autokeras.preprocessor import OneHotEncoder

import numpy as np
import dataset_provider

# TODO: Refactor the code to make it more oop, so it can be used as a component

clf = None

value_schema = """
{
    "namespace": "ai.scynet",
    "name": "value",
    "type": "record",
    "fields": [
        {
            "name": "model",
            "type": "bytes"
        }
    ]
}
"""

key_schema = """
{
    "namespace": "ai.scynet",
    "name": "key",
    "type": "record",
    "fields": [
        {
            "name": "new_best",
            "type": "boolean"
        },
        {
            "name": "id",
            "type": "long"
        },
        {
            "name": "loss",
            "type": "float"
        },
        {
            "name": "accuracy",
            "type": "float"
        }

    ]
}
"""
publish = True
producer = None # find a better way to store the producer, can't have it in self as it dissaperas :D
class ObservableSearcher(Searcher):
    def __init__(self, clf, input_shape, y_train): # input_shape: x_train.shape[1:]
        self.searcher_args = {}
        clf.y_encoder = OneHotEncoder()
        clf.y_encoder.fit(y_train)

        self.searcher_args['n_output_node'] = clf.get_n_output_node()
        self.searcher_args['input_shape'] = input_shape
        self.searcher_args['path'] = clf.path
        self.searcher_args['metric'] = clf.metric
        self.searcher_args['loss'] = clf.loss
        self.searcher_args['verbose'] = clf.verbose
        super().__init__(**self.searcher_args)
        clf.save_searcher(self)
        clf.searcher = True
        if publish:
            # Not the best solution, but I wont the code to be testable with pipenv shell in the git directory of autokeras!
            from confluent_kafka import avro
            from confluent_kafka.avro import AvroProducer
 
            value = avro.loads(value_schema)
            key = avro.loads(key_schema)

            global producer
            producer = AvroProducer({
                'bootstrap.servers': os.environ.get("BROKER", "95.158.189.52:9092"), 
                'schema.registry.url': os.environ.get("SCHEMA_REGISTRY", "http://95.158.189.52:8081"),
                'message.max.bytes': 15728640
            }, default_key_schema=key, default_value_schema=value)

            producer.produce(topic="autokeras-queen-1", key={"loss": 0, "accuracy": 0}, value={"model": b"Starting to produce models"})
            
            print("Will publish to kafka")

    def add_model(self, metric_value, loss, graph, model_id):
        print("Got the model[" + str(model_id) +  "]: " + str(loss) + " " + str(metric_value) + " ")
        if publish:
            model = graph.produce_keras_model().save("/tmp/model.h5")
            
            new_best = model_id == self.get_best_model_id():

            # print(model)
            global producer
            model_bytes = open("/tmp/model.h5", "rb").read()
            print(len(model_bytes))
            producer.produce(topic="autokeras-queen-2", key={ "new_best": new_best, "id": model_id, "loss": loss, "accuracy": metric_value}, value={"model": model_bytes}) 
            producer.flush()

        return super().add_model(metric_value, loss, graph, model_id)

def init(path, x_train, y_train):
    global clf
    # TODO: find a better way to make our own Searcher

    clf = ImageClassifier(verbose=True, augment=False, path=path, resume=True)
    searcher = ObservableSearcher(clf, x_train.shape[1:], y_train) 

def fp_fn_metrics(y_test, y):
    assert(y.shape == y_test.shape)

    fp = 0
    fn = 0

    for i in range(y.shape[0]):
        yi = int(round(y[i]))
        y_ti = int(round(y_test[i]))

        if yi != y_ti:
            print(i, yi, y_ti)
            if yi == 1:
                fp += 1
            else:
                fn += 1
    return (fp, fn)


def evaluate(x_test, y_test):
    y_test = y_test.flatten()
    
    y = clf.predict(x_test)

    sc = clf.evaluate(x_test, y_test)
    
    fp, fn = fp_fn_metrics(y_test, y)
      
    print("Diff:", y_test -y)
    print("Score %d (%d fp; %d fn / %d)" % (sc * 100, fp, fn, y_test.shape[0]))

def fit(path, x_train, y_train, x_test, y_test):
    init(path, x_train, y_train)
    clf.fit(x_train, y_train, time_limit=12 * 60 * 60)
    clf.final_fit(x_train, y_train, x_test, y_test, retrain=True)
    evaluate(x_test, y_test)

def load_model_and_evaluate(path, x, y):
    graph = pickle.load(open(path, 'rb'))

    model = graph.produce_keras_model()

    model.compile(optimizer='adam', loss='categorical_crossentropy')

    print(model.input_shape)
    print(model.output_shape)

    score = model.evaluate(x, y)
    print(score)

def run(dataset, path, should_eval):
    (x_train, y_train), (x_test, y_test) = dataset_provider.run(dataset)
    
    if not should_eval:
        if path is None: #create a folder and if it crashes, create a new one
            i = 0
            while True:
                path = 'force/'+str(i)
                i += 1
                if os.path.isdir(path):
                    continue
                os.makedirs(path)
                print("Making directory %s" % path)
                try:
                    fit(path, x_train, y_train, x_test, y_test)
                except Exception as e:
                    print(e)
                    continue
                return
        else:
            fit(path, x_train, y_train, x_test, y_test)
    else:
        init(path, x_train, y_train)
        evaluate(x_test, y_test)
        #load_model_and_evaluate(path, x_test, y_test)


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description="Trains predictor dataset on autokeras.")
    parser.add_argument('--path', default=None, help="Folder to save models and progress in, or resume training.")
    parser.add_argument('dataset', help="Pickled dataset location")
    parser.add_argument('--evaluate', dest='evaluate', action='store_true')
    parser.add_argument('--produce', dest='publish', action='store_true')
    parser.set_defaults(evaluate=False)
    parser.set_defaults(publish=False)
    
    args, _ = parser.parse_known_args()
    publish = args.publish
    run(args.dataset, args.path, args.evaluate)
