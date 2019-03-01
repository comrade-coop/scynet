#/bin/bash
sleep 1
cd ../../protocols


grpcc -a 127.0.0.1:$1 -i -p Component.proto --eval 'client.AgentStart({ egg: { componentType: "keras_executor", uuid: "e1", eggData: new Buffer("model.h5") } }, printReply)'
read -p "Press enter to continue"

grpcc -a 127.0.0.1:$1 -i -p Component.proto --eval 'client.AgentList({}, printReply)'
read -p "Press enter to continue"

grpcc -a 127.0.0.1:$1 -i -p Component.proto --eval 'client.AgentStatus({ uuid: "q1" }, printReply)'
read -p "Press enter to continue"

grpcc -a 127.0.0.1:$1 -i -p Component.proto --eval 'client.AgentStop({ uuid: "q1" }, printReply)'
read -p "Press enter to continue"

grpcc -a 127.0.0.1:$1 -i -p Component.proto --eval 'client.AgentStatus({ uuid: "q1" }, printReply)'

read -p "Press enter to continue"