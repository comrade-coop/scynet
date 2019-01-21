using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
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
        public string AgentId { get; set; }

        private readonly ConsumerConfig _config = new ConsumerConfig
        {

        };

        public InternalSubscription(string agentId)
        {
            AgentId = agentId;

            /* SubscriberThread = new Thread(() =>
            {
                while (true)
                {

                }
            });*/

            Consumer = new Consumer<string, byte[]>(_config);

        }


        public void Dispose()
        {
            Consumer?.Dispose();
        }
    }

    // TODO: write a subscription getter that trows and exception when there is no subscription.
    public class SubscriberFacade: Subscriber.SubscriberBase
    {
        private readonly ILogger _logger;
        private Dictionary<String, InternalSubscription> subscriptions;



        public SubscriberFacade(ILogger<SubscriberFacade> logger)
        {
            this._logger = logger;
        }

        public override Task<SubscriptionResponse> Subscribe(SubscriptionRequest request, ServerCallContext context)
        {
            // TODO: Get the agentId from the hatchery when ready.

            subscriptions.Add(request.Id, new InternalSubscription("NOAGENT"));
            switch (request.AgentCase)
            {
                case SubscriptionRequest.AgentOneofCase.AgentType:
                    break;
                case SubscriptionRequest.AgentOneofCase.AgetnId:
                    break;
                case SubscriptionRequest.AgentOneofCase.None:
                    break;
            }
            return Task.FromResult(new SubscriptionResponse() { AgentId = "NOAGENT" });
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
                // TODO: Find a way to implement this, without filling up the task queue, and destroying everything.
                throw new RpcException(Status.DefaultCancelled, "Currently only immediate pull's are supported.");
            }
            else
            {
                var subscription = subscriptions[request.Id];
                var response = new PullResponse();

                await Task.Run(() =>
                {
                    ConsumeResult<string, byte[]> result;

                    // TODO: There should be a better way to do this.
                    while ((result = subscription.Consumer.Consume(new TimeSpan(0))) != null)
                    {
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
                });

                return response;
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

                        responseStream.WriteAsync(new StreamingPullResponse()
                        {
                            Message = new DataMessage()
                            {
                                Data = ByteString.CopyFrom(result.Value,
                                    0,
                                    result.Value.Length),
                                Index = result.Offset.Value.ToString(),
                                Key = (uint) result.Timestamp.UnixTimestampMs,
                                Partition = (uint) result.Partition.Value,
                                PartitionKey = result.Key,
                                Redelivary = false
                            }
                        });

                }
            });
            subscription.SubscriberThread.Start();
            return;
        }
    }
}
