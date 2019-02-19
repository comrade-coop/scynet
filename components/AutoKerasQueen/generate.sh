#/bin/python3
[[ -d Scynet ]] || mkdir Scynet
python -m grpc_tools.protoc -I../../protocols --python_out=./Scynet --grpc_python_out=./Scynet ../../protocols/*.proto
