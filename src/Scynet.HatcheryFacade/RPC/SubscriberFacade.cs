using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Mime;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Threading.Tasks.Dataflow;
using Confluent.Kafka;
using Google.Protobuf;
using Grpc.Core;
using Orleans;
using Microsoft.Extensions.Logging;
using Scynet.GrainInterfaces.Agent;
using Scynet.GrainInterfaces.Registry;

namespace Scynet.HatcheryFacade.RPC
{
    class InternalSubscription : IDisposable, IEngager
    {
        public enum SubscriptionStatus : byte { Pending, Running, Errored };
        public Thread SubscriberThread { get; set; }
        public Consumer<string, byte[]> Consumer { get; set; }
        public string AgentId
        {
            get => _config.GroupId;
            set => _config.GroupId = value;
        }
        public uint BufferSize { get; }
        public SubscriptionStatus Status { get; private set; } = SubscriptionStatus.Pending;

        public BufferBlock<DataMessage> Buffer;

        private readonly ConsumerConfig _config = new ConsumerConfig
        {
            EnableAutoCommit = false,
            AutoOffsetReset = AutoOffsetResetType.Earliest
        };

        public InternalSubscription(string agentId, IEnumerable<string> brokers, uint bufferSize)
        {
            BufferSize = bufferSize;
            AgentId = agentId;

            _config.BootstrapServers = brokers.Aggregate((previous, current) => previous + "," + current);
            Buffer = new BufferBlock<DataMessage>(new DataflowBlockOptions()); // Do I need the buffer size??

        }

        public async void Start(IClusterClient clusterClient)
        {
            Consumer = new Consumer<string, byte[]>(_config);
            Consumer.OnPartitionsAssigned += (sender, list) =>
            {
                Console.WriteLine(list);
            };

            Consumer.Subscribe(AgentId);
            Consumer.Assign(new TopicPartitionOffset(AgentId, new Partition(0), new Offset(0)));
            Status = SubscriptionStatus.Pending;

            if (clusterClient != null)
            {
                try
                {
                    var thisWrap = await clusterClient.CreateObjectReference<IEngager>(this);
                    var registry = clusterClient.GetGrain<IRegistry<Guid, AgentInfo>>(0);
                    var agentInfo = await registry.Get(Guid.Parse(AgentId));
                    await agentInfo.Agent.Engage(thisWrap);
                }
                catch (Exception)
                {
                    Status = SubscriptionStatus.Errored;
                    // Happens..
                }
            }

            Status = SubscriptionStatus.Running;
        }

        public void Released(IAgent who)
        {
            Status = SubscriptionStatus.Errored;
        }

        public void Dispose()
        {
            Consumer?.Dispose();
            // SubscriberThread.Abort();
        }
    }

    // TODO: write a subscription getter that trows and exception when there is no subscription.
    public class SubscriberFacade : Subscriber.SubscriberBase
    {
        private readonly ILogger _logger;
        private readonly IEnumerable<string> _brokers;
        private readonly IClusterClient _clusterClient;
        private Dictionary<String, InternalSubscription> subscriptions = new Dictionary<string, InternalSubscription>();



        public SubscriberFacade(ILogger<SubscriberFacade> logger, IEnumerable<String> brokers, IClusterClient clusterClient)
        {
            _logger = logger;
            _brokers = brokers;
            _clusterClient = clusterClient;

        }

        public override Task<SubscriptionResponse> Subscribe(SubscriptionRequest request, ServerCallContext context)
        {
            // TODO: Get the agentId from the hatchery when ready.
            var subscription = new InternalSubscription("NOAGENT", _brokers, request.BufferSize);
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
            subscription.Start(_clusterClient);
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

        public override Task<Void> Seek(SeekRequest request, ServerCallContext context)
        {
            var subscription = subscriptions[request.Id];
            if (subscription.Status == InternalSubscription.SubscriptionStatus.Errored)
            {
                throw new RpcException(Status.DefaultCancelled, "Subscribed agent failed!");
            }

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
                SortedList<long, ConsumeResult<string, byte[]>> toBeSend = new SortedList<long, ConsumeResult<string, byte[]>>((int)subscription.BufferSize);
                long lastTimestamp = 0;
                try
                {
                    while (true) // The code here won't work if the send data is wrong.
                    {
                        context.CancellationToken.ThrowIfCancellationRequested();
                        var consumeResult = subscription.Consumer.Consume(context.CancellationToken);
                        if (consumeResult == null) continue;

                        if (consumeResult.Headers.TryGetLast("previous", out var previous))
                        {
                            if (long.TryParse(Encoding.ASCII.GetString(previous), out var previousTimestamp))
                            {
                                if (previousTimestamp == lastTimestamp)
                                {
                                    toBeSend.Add(consumeResult.Timestamp.UnixTimestampMs, consumeResult);
                                }
                                else if (previousTimestamp < lastTimestamp)
                                {
                                    toBeSend.Add(consumeResult.Timestamp.UnixTimestampMs, consumeResult);
                                    continue;
                                }
                                else
                                {
                                    _logger.LogError("This should not be possible");
                                }
                            }
                        }

                        foreach (var (key, result) in toBeSend)
                        {
                            subscription.Buffer.Post(new DataMessage()
                            {
                                Data = ByteString.CopyFrom(result.Value,
                                    0,
                                    result.Value.Length),
                                Index = result.Offset.Value.ToString(),
                                Key = (uint)result.Timestamp.UnixTimestampMs,
                                Partition = (uint)result.Partition.Value,
                                PartitionKey = result.Key,
                                Redelivary = false
                            });

                            lastTimestamp = result.Timestamp.UnixTimestampMs;
                        }

                        toBeSend.Clear();


                    }
                }
                catch (OperationCanceledException) { }
                catch (Exception e)
                {
                    _logger.LogError(e.ToString());
                }
                subscription.Buffer.Complete();

            });
            subscription.SubscriberThread.Start();

            while (!subscription.Buffer.Completion.IsCompleted)
            {
                if (subscription.Status == InternalSubscription.SubscriptionStatus.Errored)
                {
                    throw new RpcException(Status.DefaultCancelled, "Subscribed agent failed!");
                }
                var message = await subscription.Buffer.ReceiveAsync();

                await responseStream.WriteAsync(new StreamingPullResponse() { Message = message });

                subscription.Consumer.Commit(new List<TopicPartitionOffset>()
                {
                    new TopicPartitionOffset(
                        new TopicPartition(request.Id, new Partition((int) message.Partition)),
                        new Offset(Int32.Parse(message.Index))
                    )
                });
            }
        }
    }
}
