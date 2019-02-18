#/bin/python3
python -m grpc_tools.protoc -I../../protocols --python_out=./Scynet --grpc_python_out=./Scynet ../../protocols/*.proto
