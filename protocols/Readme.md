# Protocol Documentation
<a name="top"/>

## Table of Contents

- [Component.proto](#Component.proto)
    - [AgentQuery](#Scynet.AgentQuery)
    - [AgentRequest](#Scynet.AgentRequest)
    - [AgentStartRequest](#Scynet.AgentStartRequest)
    - [AgentStatusResponse](#Scynet.AgentStatusResponse)
    - [ListOfAgents](#Scynet.ListOfAgents)
  
  
  
    - [Component](#Scynet.Component)
  

- [Hatchery.proto](#Hatchery.proto)
    - [AgentRegisterRequest](#Scynet.AgentRegisterRequest)
    - [AgentRegisterResponse](#Scynet.AgentRegisterResponse)
    - [AgentStoppedEvent](#Scynet.AgentStoppedEvent)
    - [ComponentRegisterRequest](#Scynet.ComponentRegisterRequest)
    - [ComponentRegisterResponse](#Scynet.ComponentRegisterResponse)
    - [ComponentUnregisterRequest](#Scynet.ComponentUnregisterRequest)
  
  
  
    - [Hatchery](#Scynet.Hatchery)
  

- [Shared.proto](#Shared.proto)
    - [Agent](#Scynet.Agent)
    - [Void](#Scynet.Void)
  
  
  
  

- [Stream.proto](#Stream.proto)
    - [AcknowledgeRequest](#Scynet.AcknowledgeRequest)
    - [DataMessage](#Scynet.DataMessage)
    - [PublishRequest](#Scynet.PublishRequest)
    - [PublishResponse](#Scynet.PublishResponse)
    - [PullRequest](#Scynet.PullRequest)
    - [PullResponse](#Scynet.PullResponse)
    - [SeekRequest](#Scynet.SeekRequest)
    - [StreamingPullRequest](#Scynet.StreamingPullRequest)
    - [StreamingPullResponse](#Scynet.StreamingPullResponse)
    - [SubscriptionRequest](#Scynet.SubscriptionRequest)
    - [SubscriptionResponse](#Scynet.SubscriptionResponse)
    - [UnsubscribeRequest](#Scynet.UnsubscribeRequest)
  
  
  
    - [Publisher](#Scynet.Publisher)
    - [Subscriber](#Scynet.Subscriber)
  

- [Scalar Value Types](#scalar-value-types)



<a name="Component.proto"/>
<p align="right"><a href="#top">Top</a></p>

## Component.proto



<a name="Scynet.AgentQuery"/>

### AgentQuery
Used for different filters that can be applied to the query.






<a name="Scynet.AgentRequest"/>

### AgentRequest



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| uuid | [string](#string) |  | The universal identifier of the agent we want to get. |






<a name="Scynet.AgentStartRequest"/>

### AgentStartRequest



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| egg | [Agent](#Scynet.Agent) |  | The egg that will be used only when creating the agent. It contains the uuid of the agent |






<a name="Scynet.AgentStatusResponse"/>

### AgentStatusResponse



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| running | [bool](#bool) |  |  |






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
| AgentStart | [AgentStartRequest](#Scynet.AgentStartRequest) | [Void](#Scynet.AgentStartRequest) | Start running a particular agent |
| AgentStop | [AgentRequest](#Scynet.AgentRequest) | [Void](#Scynet.AgentRequest) | Stop that agent |
| AgentStatus | [AgentRequest](#Scynet.AgentRequest) | [AgentStatusResponse](#Scynet.AgentRequest) | Check the status of an agent. |
| AgentList | [AgentQuery](#Scynet.AgentQuery) | [ListOfAgents](#Scynet.AgentQuery) | Retrieve a list of running agents. |

 



<a name="Hatchery.proto"/>
<p align="right"><a href="#top">Top</a></p>

## Hatchery.proto



<a name="Scynet.AgentRegisterRequest"/>

### AgentRegisterRequest



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| agent | [Agent](#Scynet.Agent) |  |  |






<a name="Scynet.AgentRegisterResponse"/>

### AgentRegisterResponse







<a name="Scynet.AgentStoppedEvent"/>

### AgentStoppedEvent



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| agent | [Agent](#Scynet.Agent) |  |  |
| resaon | [string](#string) |  |  |
| code | [uint64](#uint64) |  |  |






<a name="Scynet.ComponentRegisterRequest"/>

### ComponentRegisterRequest



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| uuid | [string](#string) |  | The universal id of the component. |
| address | [string](#string) |  | The place we can connect to the component to call methods on it. |
| runnerType | [string](#string) | repeated | The types of eggs this component can run as agents. |






<a name="Scynet.ComponentRegisterResponse"/>

### ComponentRegisterResponse







<a name="Scynet.ComponentUnregisterRequest"/>

### ComponentUnregisterRequest



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| uuid | [string](#string) |  |  |





 

 

 


<a name="Scynet.Hatchery"/>

### Hatchery
The interface used by the components to call the hatchery.

| Method Name | Request Type | Response Type | Description |
| ----------- | ------------ | ------------- | ------------|
| RegisterComponent | [ComponentRegisterRequest](#Scynet.ComponentRegisterRequest) | [ComponentRegisterResponse](#Scynet.ComponentRegisterRequest) | Register a component so the hatchery knows it is available and can execute eggs. |
| RegisterAgent | [AgentRegisterRequest](#Scynet.AgentRegisterRequest) | [AgentRegisterResponse](#Scynet.AgentRegisterRequest) | Gives an egg to the hatchery, this way it knows that a new egg was generated by the queen or the available harvester functions. |
| UnregisterComponent | [ComponentUnregisterRequest](#Scynet.ComponentUnregisterRequest) | [Void](#Scynet.ComponentUnregisterRequest) | Used in case the whole component dies, or it was turned off in case of an upgrade. Should delete any agents owned by this component. |
| AgentStopped | [AgentStoppedEvent](#Scynet.AgentStoppedEvent) | [Void](#Scynet.AgentStoppedEvent) | Notifies that hatchery that an agent has stopped/crashed. |

 



<a name="Shared.proto"/>
<p align="right"><a href="#top">Top</a></p>

## Shared.proto



<a name="Scynet.Agent"/>

### Agent
All the data that defines an agent and is needed for executing it.


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| uuid | [string](#string) |  |  |
| eggData | [bytes](#bytes) |  | optional |
| componentType | [string](#string) |  |  |
| componentId | [string](#string) |  | id of the component in which the agent is running |
| inputs | [string](#string) | repeated |  |






<a name="Scynet.Void"/>

### Void






 

 

 

 



<a name="Stream.proto"/>
<p align="right"><a href="#top">Top</a></p>

## Stream.proto



<a name="Scynet.AcknowledgeRequest"/>

### AcknowledgeRequest



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| id | [string](#string) |  |  |
| acknowledgeMessage | [string](#string) |  |  |
| partition | [uint32](#uint32) |  |  |






<a name="Scynet.DataMessage"/>

### DataMessage



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| data | [bytes](#bytes) |  |  |
| index | [string](#string) |  | the thing needed to ack the message. |
| key | [uint64](#uint64) |  |  |
| partitionKey | [string](#string) |  |  |
| partition | [uint32](#uint32) |  |  |
| redelivary | [bool](#bool) |  |  |






<a name="Scynet.PublishRequest"/>

### PublishRequest



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| agentId | [string](#string) |  |  |
| message | [DataMessage](#Scynet.DataMessage) | repeated |  |






<a name="Scynet.PublishResponse"/>

### PublishResponse



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| index | [string](#string) | repeated |  |






<a name="Scynet.PullRequest"/>

### PullRequest



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| id | [string](#string) |  |  |
| returnImmediately | [bool](#bool) |  |  |
| maxMessages | [uint32](#uint32) |  |  |






<a name="Scynet.PullResponse"/>

### PullResponse



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| messages | [DataMessage](#Scynet.DataMessage) | repeated |  |






<a name="Scynet.SeekRequest"/>

### SeekRequest



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| id | [string](#string) |  |  |
| index | [string](#string) |  |  |
| key | [uint64](#uint64) |  |  |






<a name="Scynet.StreamingPullRequest"/>

### StreamingPullRequest



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| id | [string](#string) |  |  |






<a name="Scynet.StreamingPullResponse"/>

### StreamingPullResponse



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| message | [DataMessage](#Scynet.DataMessage) |  |  |






<a name="Scynet.SubscriptionRequest"/>

### SubscriptionRequest



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| id | [string](#string) |  | Can be the id of an agent, but also the id of onother hatchery. |
| agentType | [string](#string) |  | The type of agent we want to subscribe to. Used when subscriing to remote agents that |
| agetnId | [string](#string) |  | The agent&#39;s output that we will subscribe to. |
| strongOrdering | [bool](#bool) |  | Used to determine if we want the order of the messages to be absoulte. |
| timeout | [uint64](#uint64) |  | The subscriber will be deleted after inactivity. |
| bufferSize | [uint32](#uint32) |  | The subscriber will have a buffer to help with backbressure and/or not to overload the client. |






<a name="Scynet.SubscriptionResponse"/>

### SubscriptionResponse



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| agentId | [string](#string) |  |  |






<a name="Scynet.UnsubscribeRequest"/>

### UnsubscribeRequest



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| id | [string](#string) |  |  |





 

 

 


<a name="Scynet.Publisher"/>

### Publisher
The publishing api that will be used exclusively by the agents.

| Method Name | Request Type | Response Type | Description |
| ----------- | ------------ | ------------- | ------------|
| Publish | [PublishRequest](#Scynet.PublishRequest) | [PublishResponse](#Scynet.PublishRequest) |  |


<a name="Scynet.Subscriber"/>

### Subscriber
The subscriber api is used both by agents and other hatcheries.

| Method Name | Request Type | Response Type | Description |
| ----------- | ------------ | ------------- | ------------|
| Subscribe | [SubscriptionRequest](#Scynet.SubscriptionRequest) | [SubscriptionResponse](#Scynet.SubscriptionRequest) | Create a subscriber that we will use to listen to the messages produced by the agents. |
| Unsubscribe | [UnsubscribeRequest](#Scynet.UnsubscribeRequest) | [Void](#Scynet.UnsubscribeRequest) | Delete |
| Acknowledge | [AcknowledgeRequest](#Scynet.AcknowledgeRequest) | [Void](#Scynet.AcknowledgeRequest) | Tell the subscriber that the message was saved or transformed, and we don&#39;t want it again. |
| StreamingPull | [StreamingPullRequest](#Scynet.StreamingPullRequest) | [StreamingPullResponse](#Scynet.StreamingPullRequest) |  |
| Seek | [SeekRequest](#Scynet.SeekRequest) | [Void](#Scynet.SeekRequest) |  |

 



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

