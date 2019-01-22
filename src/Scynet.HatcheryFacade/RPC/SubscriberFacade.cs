using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using System.Threading.Tasks.Dataflow;
using Confluent.Kafka;
using Google.Protobuf;
using Grpc.Core;
using Microsoft.Extensions.Logging;

namespace Scynet.HatcheryFacade.RPC
{
    class InternalSubscription: IDisposable
    {
        public Thread SubscriberThread { get; set; }
        public Consumer<string, byte[]> Consumer { get; set; }
        public string AgentId
        {
            get => _config.GroupId;
            set => _config.GroupId = value;
        }

        public BufferBlock<DataMessage> Buffer = new BufferBlock<DataMessage>();
        
            private readonly ConsumerConfig _config = new ConsumerConfig
        {
                EnableAutoCommit = false,
                AutoOffsetReset = AutoOffsetResetType.Earliest
        };

        public InternalSubscription(string agentId, IEnumerable<string> brokers)
        {
            AgentId = agentId;

            /* SubscriberThread = new Thread(() =>
            {
                while (true)
                {

                }
            });*/
            _config.BootstrapServers = brokers.Aggregate((previous, current) => previous + "," + current);

        }

        public void Start()
        {
            Consumer = new Consumer<string, byte[]>(_config);
            Consumer.OnPartitionsAssigned += (sender, list) =>
            {
                Console.WriteLine(list);
            };

            Consumer.Subscribe(AgentId);
            
            Consumer.Assign(new TopicPartitionOffset(AgentId, new Partition(0), new Offset(0)));
            // Yes it is ok to do this, it wont return anything it will just assign.
            //Consumer.Consume(new TimeSpan(1)); // TODO: Find a way to Assign without consuming. Probably impossible.

        }


        public void Dispose()
        {
            Consumer?.Dispose();
            // SubscriberThread.Abort();
        }
    }

    // TODO: write a subscription getter that trows and exception when there is no subscription.
    public class SubscriberFacade: Subscriber.SubscriberBase
    {
        private readonly ILogger _logger;
        private readonly IEnumerable<string> _brokers;
        private Dictionary<String, InternalSubscription> subscriptions = new Dictionary<string, InternalSubscription>();

        

        public SubscriberFacade(ILogger<SubscriberFacade> logger, IEnumerable<String> brokers)
        {
            _logger = logger;
            _brokers = brokers;
        }

        public override Task<SubscriptionResponse> Subscribe(SubscriptionRequest request, ServerCallContext context)
        {
            // TODO: Get the agentId from the hatchery when ready.
            var subscription = new InternalSubscription("NOAGENT", _brokers);
            switch (request.AgentCase)
            {
                case SubscriptionRequest.AgentOneofCase.AgentType:
                    break;
                case SubscriptionRequest.AgentOneofCase.AgetnId:
                    subscription.AgentId = request.AgetnId;
                    break;
                case SubscriptionRequest.AgentOneofCase.None:
                    break;
            }
            subscriptions.Add(request.Id, subscription);
            subscription.Start();
            return Task.FromResult(new SubscriptionResponse() { AgentId = subscription.AgentId });
        }

        public override Task<Void> Unsubscribe(UnsubscribeRequest request, ServerCallContext context)
        {
            subscriptions[request.Id].Dispose();
            subscriptions.Remove(request.Id);
            return Task.FromResult(new Void());
        }

        public override Task<Void> Acknowledge(AcknowledgeRequest request, ServerCallContext context)
        {
            var subscription = subscriptions[request.Id];

            subscription.Consumer.Commit(new List<TopicPartitionOffset>()
            {
                new TopicPartitionOffset(new TopicPartition(request.Id, new Partition((int) request.Partition)),
                    new Offset(Int32.Parse(request.AcknowledgeMessage))
                    )});
            return Task.FromResult(new Void());
        }

        public override async Task<PullResponse> Pull(PullRequest request, ServerCallContext context)
        {
            if (request.ReturnImmediately)
            {
                var subscription = subscriptions[request.Id];
                var response = new PullResponse();

                // TODO: There should be a better way to do this.
                for (var i = 0; i < request.MaxMessages; i++)
                {
                    var result = await Task.Run(() =>
                        subscription.Consumer.Consume(new TimeSpan(0,0,0,10 ))
                    );
                    if(result == null) continue;
                    
                    response.Messages.Add(new DataMessage()
                    {
                        Data = ByteString.CopyFrom(result.Value,
                            0,
                            result.Value.Length),
                        Index = result.Offset.Value.ToString(),
                        Key = (uint) result.Timestamp.UnixTimestampMs,
                        Partition = (uint) result.Partition.Value,
                        PartitionKey = result.Key,
                        Redelivary = false
                    });
                }


                return response;
            }
            else
            {
                // TODO: Find a way to implement this, without filling up the task queue, and destroying everything.
                throw new RpcException(Status.DefaultCancelled, "Currently only immediate pull's are supported.");
            }
        }

        public override Task<Void> Seek(SeekRequest request, ServerCallContext context)
        {
            var subscription = subscriptions[request.Id];

            // TODO: Support more partitions.
            switch (request.TargetCase)
            {
                case SeekRequest.TargetOneofCase.Key:
                    subscription.Consumer.OffsetsForTimes(
                        new List<TopicPartitionTimestamp>
                        {
                            new TopicPartitionTimestamp(subscription.AgentId, new Partition(0),
                                new Timestamp((long) request.Key, TimestampType.CreateTime))
                        }, TimeSpan.FromMinutes(5)).ForEach((partitionOffset) => subscription.Consumer.Seek(partitionOffset));

                    break;
                case SeekRequest.TargetOneofCase.Index:
                    subscription.Consumer.Seek(new TopicPartitionOffset(subscription.AgentId, new Partition(0), new Offset(Int32.Parse(request.Index))));

                    break;
                case SeekRequest.TargetOneofCase.None:
                    throw new RpcException(Status.DefaultCancelled, "Target cannot be empty.");
            }

            return Task.FromResult(new Void());
        }

        public override async Task StreamingPull(StreamingPullRequest request, IServerStreamWriter<StreamingPullResponse> responseStream, ServerCallContext context)
        {
            var subscription = subscriptions[request.Id];
            subscription.SubscriberThread = new Thread(() =>
            {
                while (!context.CancellationToken.IsCancellationRequested)
                {
                    var result = subscription.Consumer.Consume();
                    subscription.Buffer.Post(new DataMessage()
                    {
                        Data = ByteString.CopyFrom(result.Value,
                            0,
                            result.Value.Length),
                        Index = result.Offset.Value.ToString(),
                        Key = (uint) result.Timestamp.UnixTimestampMs,
                        Partition = (uint) result.Partition.Value,
                        PartitionKey = result.Key,
                        Redelivary = false
                    });
                }

                subscription.Buffer.Complete();
            });
            subscription.SubscriberThread.Start();

            while (subscription.Buffer.Completion.IsCompleted)
            {
                var message = await subscription.Buffer.ReceiveAsync();
                await responseStream.WriteAsync(new StreamingPullResponse() {Message = message});
            }

            return;
        }
    }
}
