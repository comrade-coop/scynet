﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Mime;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using Confluent.Kafka;
using Google.Protobuf;
using Grpc.Core;
using Orleans;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Hosting;
using Scynet.GrainInterfaces.Facade;
using Scynet.GrainInterfaces.Registry;
using Scynet.GrainInterfaces.Agent;

namespace Scynet.HatcheryFacade.RPC
{
    public class SubscriberClient : IFacade, IHostedService
    {
        private readonly ILogger _logger;
        private readonly IEnumerable<string> _brokers;

        private readonly IClusterClient ClusterClient;
        private readonly Guid FacadeGuid = new Guid();
        private Timer Timer;

        private Dictionary<string, CancellationTokenSource> cancellationTokens = new Dictionary<string, CancellationTokenSource>();
        private Dictionary<string, Channel> channels = new Dictionary<string, Channel>();

        public SubscriberClient(ILogger<SubscriberClient> logger, IEnumerable<String> brokers, IClusterClient clusterClient)
        {
            _logger = logger;
            _brokers = brokers;
            ClusterClient = clusterClient;

            Register();
        }

        public async void Register()
        {
            var facadeWrap = await ClusterClient.CreateObjectReference<IFacade>(this);

            var registry = ClusterClient.GetGrain<IRegistry<Guid, FacadeInfo>>(0);

            _logger.LogInformation("Registered!");
            await registry.Register(FacadeGuid, new FacadeInfo()
            {
                Facade = facadeWrap,
                LastUpdate = DateTime.Now,
            });

            Timer = new Timer(async _ =>
            {
                _logger.LogInformation("Registered!");
                await registry.Register(FacadeGuid, new FacadeInfo()
                {
                    Facade = facadeWrap,
                    LastUpdate = DateTime.Now,
                });
            }, null, TimeSpan.FromSeconds(15), TimeSpan.FromSeconds(15));
        }

        public async void Start(IExternalAgent agent)
        {
            Subscribe(await agent.GetAddress(), agent.GetPrimaryKey().ToString());
        }

        public async void Stop(IExternalAgent agent)
        {
            Unsubscribe(await agent.GetAddress(), agent.GetPrimaryKey().ToString());
        }

        public Task StartAsync(CancellationToken cancellationToken)
        {
            return Task.CompletedTask;
        }

        public Task StopAsync(CancellationToken cancellationToken)
        {
            return Task.CompletedTask;
        }

        public async void Subscribe(string address, string agentId)
        {
            if (cancellationTokens.ContainsKey(address + "/" + agentId))
            {
                return;
            }

            var cts = new CancellationTokenSource();
            cancellationTokens[address + "/" + agentId] = cts;

            if (!channels.ContainsKey(address))
            {
                channels[address] = new Channel(address, ChannelCredentials.Insecure);
            }

            var client = new Subscriber.SubscriberClient(channels[address]);
            var producer = new Producer<string, byte[]>(new ProducerConfig { BootstrapServers = string.Join(";", _brokers) });

            var subscriptionId = Guid.NewGuid().ToString();

            try
            {
                await client.SubscribeAsync(new SubscriptionRequest() { Id = subscriptionId, AgetnId = agentId, BufferSize = 32 }, null, null, cts.Token);

                while (true)
                {
                    Console.WriteLine("Well, well, here we go!");
                    cts.Token.ThrowIfCancellationRequested();
                    using (var pull = client.StreamingPull(new StreamingPullRequest() { Id = subscriptionId }))
                    {
                        while (await pull.ResponseStream.MoveNext(cts.Token))
                        {
                            var message = pull.ResponseStream.Current.Message;

                            await producer.ProduceAsync(agentId, new Message<string, byte[]>()
                            {
                                Key = message.PartitionKey,
                                Value = message.Data.ToByteArray(),
                                Timestamp = new Timestamp((long)message.Key, TimestampType.CreateTime),
                            }, cts.Token);
                        }
                    }
                }
            }
            catch (RpcException re)
            {
                var registry = ClusterClient.GetGrain<IRegistry<Guid, AgentInfo>>(0);
                var agentInfo = await registry.Get(Guid.Parse(agentId));
                await agentInfo.Agent.ReleaseAll(); // :(
                _logger.LogError(re.ToString());
                return;
            }
        }

        public void Unsubscribe(string address, string agentId)
        {
            cancellationTokens[address + "/" + agentId].Cancel();
            cancellationTokens[address + "/" + agentId].Dispose();
            cancellationTokens.Remove(address + "/" + agentId);
        }
    }
}
