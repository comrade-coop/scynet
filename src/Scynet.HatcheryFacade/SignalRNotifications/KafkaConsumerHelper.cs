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

namespace Scynet.HatcheryFacade.SignalRNotifications
{

    public class KafkaConsumerHelper : IHostedService
    {
        private readonly ILogger _logger;

        private readonly IClusterClient _clusterClient;
        private readonly IConfiguration _configuration;
        private readonly ICollection<Thread> _threads = new List<Thread>();

        public KafkaConsumerHelper(ILogger<KafkaConsumerHelper> logger, IClusterClient clusterClient,
            IConfiguration configuration, IHubContext<NotifyHub, INotifyHubClient> hubContext)
        {
            _logger = logger;
            _clusterClient = clusterClient;
            _configuration = configuration;
            //StartX("localhost:9092", "331d591b-184d-4e7c-b075-9841181c05c1");
        }

        public void ConsumeStream(string channel, CancellationToken cancellationToken, Action<ConsumeResult<string, byte[]>> callback)
        {
            var config = new ConsumerConfig();
            _configuration.GetSection("Kafka").GetSection("ConsumerConfig").Bind(config);

            config.EnableAutoCommit = false;
            config.AutoOffsetReset = AutoOffsetResetType.Earliest;
            config.GroupId = Guid.NewGuid().ToString();

            var Consumer = new Consumer<string, byte[]>(config);
            Consumer.Subscribe(channel);

            var subscriberThread = new Thread(() =>
            {
                try
                {
                    while (true)
                    {
                        cancellationToken.ThrowIfCancellationRequested();
                        var consumeResult = Consumer.Consume(cancellationToken);
                        try
                        {
                            callback(consumeResult);
                        }
                        catch (Exception e)
                        {
                            Console.WriteLine(e);
                        }
                    }
                }
                catch (OperationCanceledException)
                {
                    return;
                }
                catch (Exception e)
                {
                    Console.WriteLine(e);
                }
            });

            subscriberThread.Start();
            _threads.Add(subscriberThread);
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
