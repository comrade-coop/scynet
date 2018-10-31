import pickle
import os, io, sys
import numpy as np
import json
import zlib
from confluent_kafka import avro
from confluent_kafka.avro import AvroProducer
from time import sleep

value_schema = """
{
    "namespace": "ai.scynet",
    "name": "value",
    "type": "record",
    "fields": [
        {
            "name": "dataframe",
            "type": {
                "type": "array",
		"items": {
	                "type": "array",
        		"items": {
		                "type": "array",
 	        		"items" : "double"					
        		}
		}
            } 
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
            "name": "index",
            "type": "int"
        }

    ]
}
"""



def sizeof_fmt(num, suffix='B'):
    for unit in ['','Ki','Mi','Gi','Ti','Pi','Ei','Zi']:
        if abs(num) < 1024.0:
            return "%3.1f%s%s" % (num, unit, suffix)
        num /= 1024.0
    return "%.1f%s%s" % (num, 'Yi', suffix)

def run(dataset):
    value = avro.loads(value_schema)
    key = avro.loads(key_schema)

    producer = AvroProducer({
        'bootstrap.servers': os.environ.get("BROKER", "127.0.0.1:9092"), 
        'schema.registry.url': os.environ.get("SCHEMA_REGISTRY", "http://127.0.0.1:8081"),
        # 'default.topic.config': {'compression.codec': 'snappy'}
        'compression.codec': 'snappy',
        'message.max.bytes': 15728640
    }, default_key_schema=key, default_value_schema=value)
    
    with open(dataset, "rb") as file:
        dataset = pickle.load(file)
        x_test =  dataset[1]["dataset"]
        one_element = x_test[0]
        # print(one_element.getbuffer())
        # result = io.BytesIO()
        # np.save(result, one_element)
        # result.seek(0)
        # r = result.read()
        # original_size = len(r)
        # new_size = len(zlib.compress(r))
        # print(sizeof_fmt( original_size ))
        # print(sizeof_fmt( new_size ))
        # print(original_size/new_size)

        # result.seek(0)

        # print(np.load(result).shape)

        # print(one_element.shape)

        for idx, el in enumerate( x_test ):
            print("Uploading: %d" %(idx))
            #result = io.BytesIO()
            # np.save(result, el)
            # result.seek(0)
            # data = zlib.compress( result.read() )
            # data = zlib.compress( result.read() )
            producer.produce(topic="balance-lastSeen", key={'index': idx}, value={"dataframe": el.tolist() })
            # sleep(100);


if __name__ == "__main__":
    if len(sys.argv) == 2: 
        run(sys.argv[1])
    else:
        print(f"run with:\n {sys.argv[0]} dataset")
