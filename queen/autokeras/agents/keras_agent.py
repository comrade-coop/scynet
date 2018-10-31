from keras.models import load_model
import pickle 
import numpy as np
import os
import confluent_kafka as kafka
from confluent_kafka import KafkaError, TopicPartition
from confluent_kafka.avro import AvroConsumer, AvroProducer
from confluent_kafka.avro.serializer import SerializerError
from confluent_kafka import avro

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
                    "items": "double"
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


def run(model_path):
    value = avro.loads(value_schema)
    key = avro.loads(key_schema)

    producer = AvroProducer({
        'bootstrap.servers': os.environ.get("BROKER", "127.0.0.1:9092"), 
        'schema.registry.url': os.environ.get("SCHEMA_REGISTRY", "http://127.0.0.1:8081"),
        'compression.codec': 'snappy',
    }, default_key_schema=key, default_value_schema=value)


    dir_path = os.path.dirname(os.path.realpath(__file__)) + "/" #TODO: This will be input from kafka, and model storage

    consumer = AvroConsumer({
        'bootstrap.servers': os.environ.get("BROKER", "127.0.0.1:9092"),
        'group.id': 'agent_keras',
        'schema.registry.url': 'http://127.0.0.1:8081',
        'auto.offset.reset': 'smallest'
    })

    for partition in consumer.list_topics().topics["balance-lastSeen"].partitions:
        print("Subscribing to: %s " % (partition) )
        consumer.assign([ TopicPartition("balance-lastSeen", partition, kafka.OFFSET_BEGINNING) ])


    model = load_model(dir_path + model_path)
    model.compile(optimizer='adam', loss='categorical_crossentropy', metrics=['accuracy'])
        
    # consumer.subscribe([ "balance-lastSeen" ])
    # consumer.assign([ TopicPartition("balance-lastSeen", 0, kafka.OFFSET_STORED) ])

    poll_time = 0
    while True:
        try:
            msg = consumer.poll(poll_time)

        except SerializerError as e:
            print("Message deserialization failed for {}: {}".format(msg, e))
            break

        if msg is None:
            poll_time = 10
            continue

        # print(msg.value())

        if msg.error():
            if msg.error().code() == KafkaError._PARTITION_EOF:
                continue
            else:
                print(msg.error())
                break

        poll_time = 0
        prediction = model.predict(np.asarray([ msg.value()["dataframe"] ]))
        consumer.commit(msg)

        print(prediction[0])
        producer.produce(topic="agent0", key=msg.key(), value={ "dataframe": prediction.tolist() })
        
    consumer.close()

if __name__ == "__main__":
    import sys
    if len(sys.argv) == 2:
        run(sys.argv[1])
    else:
        print(f"{sys.argv[0]} model.h5")
