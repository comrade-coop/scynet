using Confluent.Kafka;
using Microsoft.AspNetCore.SignalR;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using Orleans;
using Scynet.HatcheryFacade.SignalRNotifications;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace Scynet.HatcheryFacade.CustomKafkaConsumer
{
    public class KafkaConsumerFacade : IHostedService
    {
        private readonly ILogger _logger;

        private readonly IClusterClient _clusterClient;
        private readonly IConfiguration _configuration;
        private Dictionary<String, KafkaConsumer> Subscriptions = new Dictionary<string, KafkaConsumer>();

        public KafkaConsumerFacade(ILogger<KafkaConsumerFacade> logger, IClusterClient clusterClient,
            IConfiguration configuration, IHubContext<NotifyHub, IHubClient> hubContext)
        {
            _logger = logger;
            _clusterClient = clusterClient;
            _configuration = configuration;
            //StartX("localhost:9092", "331d591b-184d-4e7c-b075-9841181c05c1");
        }

        public void StartConsuming(string agentUuid, Action<ConsumeResult<string, byte[]>> callback)
        {
            var c = _configuration.GetSection("Kafka");
            
            if(!Subscriptions.ContainsKey(agentUuid))
            {
                var newSubscription = new KafkaConsumer(agentUuid, _configuration.GetSection("Kafka"));
                Subscriptions.Add(agentUuid, newSubscription);
            } else
            {
                Subscriptions[agentUuid] = new KafkaConsumer(agentUuid, _configuration.GetSection("Kafka"));
            }

            var subscription = Subscriptions[agentUuid];


            subscription.Consumer.Subscribe(agentUuid);

            subscription.SubscriberThread = new Thread(() =>
            {
                while (true)
                {
                    var consumeResult = subscription.Consumer.Consume();
                    callback(consumeResult);
                }
            });

            subscription.SubscriberThread.Start();
        }

        public void StopConsuming(string agentUuid)
        {
            var subscription = Subscriptions[agentUuid];
            subscription.Dispose();
            Subscriptions.Remove(agentUuid);
        }

        public Task StartAsync(CancellationToken cancellationToken)
        {
            return Task.CompletedTask;
        }

        public Task StopAsync(CancellationToken cancellationToken)
        {
            return Task.CompletedTask;
        }
    }
}
