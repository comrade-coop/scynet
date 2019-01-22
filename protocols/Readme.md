# Protocol Documentation
<a name="top"/>

## Table of Contents

- [Component.proto](#Component.proto)
    - [AgentQuery](#Scynet.AgentQuery)
    - [AgentRequest](#Scynet.AgentRequest)
    - [AgentStartRequest](#Scynet.AgentStartRequest)
    - [AgentStatusResponse](#Scynet.AgentStatusResponse)
    - [ListOfAgents](#Scynet.ListOfAgents)
  
  
  
    - [ComponentApi](#Scynet.ComponentApi)
  

- [Hatchery.proto](#Hatchery.proto)
    - [AgentStopedEvent](#Scynet.AgentStopedEvent)
    - [AgentSubscriptionRequest](#Scynet.AgentSubscriptionRequest)
    - [AgnentQueryRequest](#Scynet.AgnentQueryRequest)
    - [ComponentRegisterRequest](#Scynet.ComponentRegisterRequest)
    - [ComponentRegisterResponse](#Scynet.ComponentRegisterResponse)
    - [ComponentRequest](#Scynet.ComponentRequest)
    - [EggQueryRequest](#Scynet.EggQueryRequest)
    - [EggRegisterRequest](#Scynet.EggRegisterRequest)
    - [EggRegisterResponse](#Scynet.EggRegisterResponse)
  
  
  
    - [HatcheryApi](#Scynet.HatcheryApi)
  

- [Shared.proto](#Shared.proto)
    - [Agent](#Scynet.Agent)
    - [Component](#Scynet.Component)
    - [Egg](#Scynet.Egg)
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
    - [Subscription](#Scynet.Subscription)
  
  
  
    - [PublisherApi](#Scynet.PublisherApi)
    - [SubscriberApi](#Scynet.SubscriberApi)
  

- [Scalar Value Types](#scalar-value-types)



<a name="Component.proto"/>
<p align="right"><a href="#top">Top</a></p>

## Component.proto



<a name="Scynet.AgentQuery"/>

### AgentQuery
Used for different filters that can abe applied to the query.






<a name="Scynet.AgentRequest"/>

### AgentRequest



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| uuid | [string](#string) |  | The universal identifier of the agent we want to get. |






<a name="Scynet.AgentStartRequest"/>

### AgentStartRequest



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| uuid | [string](#string) |  | The universal identifier of the agent we want to interface. |
| egg | [Egg](#Scynet.Egg) |  | The egg that will be used only when creating the agent, and ignoered otherwise. |






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





 

 

 


<a name="Scynet.ComponentApi"/>

### ComponentApi
The Api whic is implemented by the different components. 
It is caled by the hatchery to instantiate the agents.

| Method Name | Request Type | Response Type | Description |
| ----------- | ------------ | ------------- | ------------|
| AgentStart | [AgentStartRequest](#Scynet.AgentStartRequest) | [Void](#Scynet.AgentStartRequest) | Start runngin a particular agent |
| AgentStop | [AgentRequest](#Scynet.AgentRequest) | [Void](#Scynet.AgentRequest) | Stop that agent |
| AgentStatus | [AgentRequest](#Scynet.AgentRequest) | [AgentStatusResponse](#Scynet.AgentRequest) | Check the status of an agent. |
| AgentList | [AgentQuery](#Scynet.AgentQuery) | [ListOfAgents](#Scynet.AgentQuery) | Retrieve a list of agents. |

 



<a name="Hatchery.proto"/>
<p align="right"><a href="#top">Top</a></p>

## Hatchery.proto



<a name="Scynet.AgentStopedEvent"/>

### AgentStopedEvent



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| agent | [Agent](#Scynet.Agent) |  |  |
| resaon | [string](#string) |  |  |
| code | [uint64](#uint64) |  |  |






<a name="Scynet.AgentSubscriptionRequest"/>

### AgentSubscriptionRequest



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| agentType | [string](#string) |  | The type of the agent we want to subscribe to |






<a name="Scynet.AgnentQueryRequest"/>

### AgnentQueryRequest



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| uuid | [string](#string) |  |  |






<a name="Scynet.ComponentRegisterRequest"/>

### ComponentRegisterRequest



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| address | [string](#string) |  | The plase we can connect to the component to call methods on it. |
| component | [Component](#Scynet.Component) |  |  |






<a name="Scynet.ComponentRegisterResponse"/>

### ComponentRegisterResponse







<a name="Scynet.ComponentRequest"/>

### ComponentRequest



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| uuid | [string](#string) |  |  |






<a name="Scynet.EggQueryRequest"/>

### EggQueryRequest



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| uuid | [string](#string) |  |  |






<a name="Scynet.EggRegisterRequest"/>

### EggRegisterRequest



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| uuid | [string](#string) |  | The universal id of the egg. |
| egg | [Egg](#Scynet.Egg) |  |  |






<a name="Scynet.EggRegisterResponse"/>

### EggRegisterResponse






 

 

 


<a name="Scynet.HatcheryApi"/>

### HatcheryApi
The interface used by the components to call the hatchery.

| Method Name | Request Type | Response Type | Description |
| ----------- | ------------ | ------------- | ------------|
| RegisterComponent | [ComponentRegisterRequest](#Scynet.ComponentRegisterRequest) | [ComponentRegisterResponse](#Scynet.ComponentRegisterRequest) | Register a component so the hatchery knows it is available and can execute eggs. |
| RegisterEgg | [EggRegisterRequest](#Scynet.EggRegisterRequest) | [EggRegisterResponse](#Scynet.EggRegisterRequest) | Gives an egg to the hatchery, this way it knows that a new egg was generated by the queen or the available harvester functions. I think there should be a way to garbege collec eggs that are no longer usefull but ... |
| FindComponent | [ComponentRequest](#Scynet.ComponentRequest) | [Component](#Scynet.ComponentRequest) |  |
| FindEgg | [EggQueryRequest](#Scynet.EggQueryRequest) | [Egg](#Scynet.EggQueryRequest) |  |
| UnregisterComponent | [ComponentRequest](#Scynet.ComponentRequest) | [Void](#Scynet.ComponentRequest) | Used in case the whole component dies, or it was turned off in case of an ubgrade. Should delete any agents owned by this component. |
| HandleAgentStop | [AgentStopedEvent](#Scynet.AgentStopedEvent) | [Void](#Scynet.AgentStopedEvent) | Deletes an agent. If an agent crashes or it was destroyd by the comonent. |

 



<a name="Shared.proto"/>
<p align="right"><a href="#top">Top</a></p>

## Shared.proto



<a name="Scynet.Agent"/>

### Agent



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| uuid | [string](#string) |  |  |
| type | [string](#string) |  | the type |
| componentId | [string](#string) |  |  |
| running | [bool](#bool) |  |  |






<a name="Scynet.Component"/>

### Component



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| uuid | [string](#string) |  | The universal id of the component. |
| runnerType | [string](#string) | repeated | The types of eggs this component can run as agents. |






<a name="Scynet.Egg"/>

### Egg
All the data that defines an agent and is needed for executing it.


| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| uuid | [string](#string) |  |  |
| data | [bytes](#bytes) |  |  |
| type | [string](#string) |  |  |
| componentId | [string](#string) |  |  |
| outputType | [string](#string) |  |  |
| inputsTypes | [string](#string) | repeated |  |






<a name="Scynet.Void"/>

### Void






 

 

 

 



<a name="Stream.proto"/>
<p align="right"><a href="#top">Top</a></p>

## Stream.proto



<a name="Scynet.AcknowledgeRequest"/>

### AcknowledgeRequest



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| subscriber | [string](#string) |  |  |
| acknowledgeMessage | [string](#string) |  |  |






<a name="Scynet.DataMessage"/>

### DataMessage



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| data | [bytes](#bytes) |  |  |
| index | [string](#string) |  | the thing needed to ack the message. |
| key | [uint64](#uint64) |  |  |
| partitionKey | [string](#string) |  |  |
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
| subscriber | [string](#string) |  |  |
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
| subscriber | [string](#string) |  |  |
| index | [string](#string) |  |  |
| key | [uint64](#uint64) |  |  |






<a name="Scynet.StreamingPullRequest"/>

### StreamingPullRequest



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| subscriber | [string](#string) |  |  |
| messagesAck | [string](#string) |  |  |






<a name="Scynet.StreamingPullResponse"/>

### StreamingPullResponse



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| messages | [DataMessage](#Scynet.DataMessage) | repeated |  |






<a name="Scynet.Subscription"/>

### Subscription



| Field | Type | Label | Description |
| ----- | ---- | ----- | ----------- |
| id | [string](#string) |  | Can be the id of an agent, but also the id of onother hatchery. |
| agentType | [string](#string) |  | The type of agent we want to subscribe to. Used when subscriing to remote agents that |
| agetnId | [string](#string) |  | The agent&#39;s output that we will subscribe to. |
| strongOrdering | [bool](#bool) |  | Used to determine if we want the order of the messages to be absoulte. |
| timeout | [uint64](#uint64) |  | The subscriber will be deleted after inactivity. |
| bufferSize | [uint32](#uint32) |  | The subscriber will have a buffer to help with backbressure and/or not to overload the client. |





 

 

 


<a name="Scynet.PublisherApi"/>

### PublisherApi
The publishing api that will be used exclusively by the agents.

| Method Name | Request Type | Response Type | Description |
| ----------- | ------------ | ------------- | ------------|
| Publish | [PublishRequest](#Scynet.PublishRequest) | [PublishResponse](#Scynet.PublishRequest) |  |


<a name="Scynet.SubscriberApi"/>

### SubscriberApi
The subscriber api is used both by agnets and other hatcheries.

| Method Name | Request Type | Response Type | Description |
| ----------- | ------------ | ------------- | ------------|
| Subscrive | [Subscription](#Scynet.Subscription) | [Subscription](#Scynet.Subscription) | Create a subscriber that we will use to listen to the messages produced by the agents. |
| Unsubscribe | [Subscription](#Scynet.Subscription) | [Void](#Scynet.Subscription) | Delete |
| Acknowledge | [AcknowledgeRequest](#Scynet.AcknowledgeRequest) | [Void](#Scynet.AcknowledgeRequest) | Tell the subscriber that the message was saved or transformed, and we don&#39;t want it again. |
| Pull | [PullRequest](#Scynet.PullRequest) | [PullResponse](#Scynet.PullRequest) | Get the newest messages. |
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

