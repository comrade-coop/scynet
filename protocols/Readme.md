# Protocol Documentation
<a name="top"/>

## Table of Contents

- [Component.proto](#Component.proto)
    - [AgentQuery](#Scynet.AgentQuery)
    - [AgentRequest](#Scynet.AgentRequest)
    - [AgentResponse](#Scynet.AgentResponse)
    - [ListOfAgents](#Scynet.ListOfAgents)
  
  
  
    - [Component](#Scynet.Component)
  

- [Hatchery.proto](#Hatchery.proto)
    - [AgentSubscriptionRequest](#Scynet.AgentSubscriptionRequest)
    - [AgentSubscriptionResponse](#Scynet.AgentSubscriptionResponse)
    - [ComponentRegisterRequest](#Scynet.ComponentRegisterRequest)
    - [ComponentRegisterResponse](#Scynet.ComponentRegisterResponse)
    - [EggRegisterRequest](#Scynet.EggRegisterRequest)
    - [EggRegisterResponse](#Scynet.EggRegisterResponse)
    - [StreamReqest](#Scynet.StreamReqest)
    - [StreamRespose](#Scynet.StreamRespose)
    - [TopicRegisterRequest](#Scynet.TopicRegisterRequest)
    - [TopicRegisterRequest.ConfigEntry](#Scynet.TopicRegisterRequest.ConfigEntry)
    - [TopicRegisterRespnse](#Scynet.TopicRegisterRespnse)
  
  
  
    - [Hatchery](#Scynet.Hatchery)
  

- [Shared.proto](#Shared.proto)
    - [Agent](#Scynet.Agent)
    - [Egg](#Scynet.Egg)
    - [Stream](#Scynet.Stream)
    - [Topic](#Scynet.Topic)
  
    - [AgentCondition](#Scynet.AgentCondition)
    - [StreamDirection](#Scynet.StreamDirection)
  
  
  

- [Scalar Value Types](#scalar-value-types)



<a name="Component.proto"/>
<p align="right"><a href="#top">Top</a></p>

## Component.proto



<a name="Scynet.AgentQuery"/>

### AgentQuery
Used for different filters that can abe applied to the query.


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| status | [AgentCondition](#Scynet.AgentCondition) |  | Get only agents with certain condition. |






<a name="Scynet.AgentRequest"/>

### AgentRequest



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| uuid | [string](#string) |  | The universal identifier of the agent we want to interface. |
| egg | [Egg](#Scynet.Egg) |  | The egg that will be used only when creating the agent, and ignoered otherwise. |






<a name="Scynet.AgentResponse"/>

### AgentResponse
Return the agent after every request.


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| agent | [Agent](#Scynet.Agent) |  |  |






<a name="Scynet.ListOfAgents"/>

### ListOfAgents
The list of avalable agents.


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| agents | [Agent](#Scynet.Agent) | repeated |  |





 

 

 


<a name="Scynet.Component"/>

### Component
The Api whic is implemented by the different components. 
It is caled by the hatchery to instantiate the agents.

| Method Name | Request Type | Response Type | Description |
| ----------- | ------------ | ------------- | ------------|
| AgentStart | [AgentRequest](#Scynet.AgentRequest) | [AgentResponse](#Scynet.AgentRequest) | Start runngin a particular agent |
| AgentStop | [AgentRequest](#Scynet.AgentRequest) | [AgentResponse](#Scynet.AgentRequest) | Stop that agent |
| AgentStatus | [AgentRequest](#Scynet.AgentRequest) | [AgentResponse](#Scynet.AgentRequest) | Check the status of an agent. |
| AgentList | [AgentQuery](#Scynet.AgentQuery) | [ListOfAgents](#Scynet.AgentQuery) | Retrieve a list of agents. |

 



<a name="Hatchery.proto"/>
<p align="right"><a href="#top">Top</a></p>

## Hatchery.proto



<a name="Scynet.AgentSubscriptionRequest"/>

### AgentSubscriptionRequest



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| agentType | [string](#string) |  | The type of the agent we want to subscribe to |






<a name="Scynet.AgentSubscriptionResponse"/>

### AgentSubscriptionResponse



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| topic | [Topic](#Scynet.Topic) |  | The topic which we can listen to for the agent output. |






<a name="Scynet.ComponentRegisterRequest"/>

### ComponentRegisterRequest



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| uuid | [string](#string) |  | The universal id of the component. |
| runnerType | [string](#string) |  | The types of eggs this component can run as agents. |
| canExecuteNewEggs | [bool](#bool) |  | Can it excute agents that it hasn&#39;t registered. |






<a name="Scynet.ComponentRegisterResponse"/>

### ComponentRegisterResponse







<a name="Scynet.EggRegisterRequest"/>

### EggRegisterRequest



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| uuid | [string](#string) |  | The universal id of the egg. |
| egg | [Egg](#Scynet.Egg) |  |  |






<a name="Scynet.EggRegisterResponse"/>

### EggRegisterResponse







<a name="Scynet.StreamReqest"/>

### StreamReqest



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| topic | [string](#string) |  | The topic name to search for. |






<a name="Scynet.StreamRespose"/>

### StreamRespose



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| _stream | [Stream](#Scynet.Stream) |  | The stream |
| owner | [Agent](#Scynet.Agent) |  | The agent who uses this stream |






<a name="Scynet.TopicRegisterRequest"/>

### TopicRegisterRequest



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| topic | [Topic](#Scynet.Topic) |  | The topic name to register and create a stream. |
| config | [TopicRegisterRequest.ConfigEntry](#Scynet.TopicRegisterRequest.ConfigEntry) | repeated | The configuration for the topic. |






<a name="Scynet.TopicRegisterRequest.ConfigEntry"/>

### TopicRegisterRequest.ConfigEntry



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| key | [string](#string) |  |  |
| value | [string](#string) |  |  |






<a name="Scynet.TopicRegisterRespnse"/>

### TopicRegisterRespnse






 

 

 


<a name="Scynet.Hatchery"/>

### Hatchery
The interface used by the components to call the hatchery.

| Method Name | Request Type | Response Type | Description |
| ----------- | ------------ | ------------- | ------------|
| RegisterComponent | [ComponentRegisterRequest](#Scynet.ComponentRegisterRequest) | [ComponentRegisterResponse](#Scynet.ComponentRegisterRequest) | Register a component so the hatchery knows it is available and can execute eggs. |
| RegisterTopic | [TopicRegisterRequest](#Scynet.TopicRegisterRequest) | [TopicRegisterRespnse](#Scynet.TopicRegisterRequest) | Tells the hatchery that a topic is going to be used by one of the agents to push data to. This will most like result in creating a stream. |
| RegisterEgg | [EggRegisterRequest](#Scynet.EggRegisterRequest) | [EggRegisterResponse](#Scynet.EggRegisterRequest) | Gives an egg to the hatchery, this way it knows that a new egg was generated by the queen or the available harvester functions. |
| SubscribeAgent | [AgentSubscriptionRequest](#Scynet.AgentSubscriptionRequest) | [AgentSubscriptionResponse](#Scynet.AgentSubscriptionRequest) | Get the resulting stream of an internal/external agent to use by our agent. |
| GetStream | [StreamReqest](#Scynet.StreamReqest) | [StreamRespose](#Scynet.StreamReqest) | Check to see if a stream is used by any agent, and in the case it is get the stream and the agent. |

 



<a name="Shared.proto"/>
<p align="right"><a href="#top">Top</a></p>

## Shared.proto



<a name="Scynet.Agent"/>

### Agent



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| uuid | [string](#string) |  |  |
| type | [string](#string) |  | the type |
| status | [AgentCondition](#Scynet.AgentCondition) |  |  |
| streams | [Stream](#Scynet.Stream) | repeated |  |






<a name="Scynet.Egg"/>

### Egg
All the data that defines an agent and is needed for executing it.


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| data | [bytes](#bytes) |  |  |
| component | [string](#string) |  |  |
| streams | [Stream](#Scynet.Stream) | repeated |  |






<a name="Scynet.Stream"/>

### Stream
The stream representation for an agent.


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| direction | [StreamDirection](#Scynet.StreamDirection) |  | The direction of the data. |
| topic | [Topic](#Scynet.Topic) |  | The topic used to facilitate the data transfer. |






<a name="Scynet.Topic"/>

### Topic



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| name | [string](#string) |  | The name of the topic. |
| type | [string](#string) |  | The type of messages that can be send to that topic. |





 


<a name="Scynet.AgentCondition"/>

### AgentCondition
The state in which an agent is.

| Name | Number | Description |
| ---- | ------ | ----------- |
| NonExistent | 0 |  |
| Stoped | 1 |  |
| Running | 2 |  |



<a name="Scynet.StreamDirection"/>

### StreamDirection
Indicates if agents pull(Out) or push(In) data to the stream.

| Name | Number | Description |
| ---- | ------ | ----------- |
| In | 0 | pull |
| Out | 1 | push |
| Both | 2 | both |


 

 

 



## Scalar Value Types

| .proto Type | Notes | C++ Type | Java Type | Python Type |
| ----------- | ----- | -------- | --------- | ----------- |
| <a name="double" /> double |  | double | double | float |
| <a name="float" /> float |  | float | float | float |
| <a name="int32" /> int32 | Uses variable-length encoding. Inefficient for encoding negative numbers – if your field is likely to have negative values, use sint32 instead. | int32 | int | int |
| <a name="int64" /> int64 | Uses variable-length encoding. Inefficient for encoding negative numbers – if your field is likely to have negative values, use sint64 instead. | int64 | long | int/long |
| <a name="uint32" /> uint32 | Uses variable-length encoding. | uint32 | int | int/long |
| <a name="uint64" /> uint64 | Uses variable-length encoding. | uint64 | long | int/long |
| <a name="sint32" /> sint32 | Uses variable-length encoding. Signed int value. These more efficiently encode negative numbers than regular int32s. | int32 | int | int |
| <a name="sint64" /> sint64 | Uses variable-length encoding. Signed int value. These more efficiently encode negative numbers than regular int64s. | int64 | long | int/long |
| <a name="fixed32" /> fixed32 | Always four bytes. More efficient than uint32 if values are often greater than 2^28. | uint32 | int | int |
| <a name="fixed64" /> fixed64 | Always eight bytes. More efficient than uint64 if values are often greater than 2^56. | uint64 | long | int/long |
| <a name="sfixed32" /> sfixed32 | Always four bytes. | int32 | int | int |
| <a name="sfixed64" /> sfixed64 | Always eight bytes. | int64 | long | int/long |
| <a name="bool" /> bool |  | bool | boolean | boolean |
| <a name="string" /> string | A string must always contain UTF-8 encoded or 7-bit ASCII text. | string | String | str/unicode |
| <a name="bytes" /> bytes | May contain any arbitrary sequence of bytes. | string | ByteString | str |

