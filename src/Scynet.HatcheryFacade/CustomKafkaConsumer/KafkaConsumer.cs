using Confluent.Kafka;
using Microsoft.Extensions.Configuration;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;

namespace Scynet.HatcheryFacade.CustomKafkaConsumer
{
    public class KafkaConsumer
    {
        public Thread SubscriberThread { get; set; }
        public Consumer<string, byte[]> Consumer { get; set; }
        public string GroupId { get; set; }
        /*public string GroupId
        {
            get => _config.GroupId;
            set => _config.GroupId = value;
        }*/

        public string AgentId { get; set; }
        private readonly ConsumerConfig _config;


        public KafkaConsumer(string agentUuid, IConfiguration config)
        {
            this.GroupId = agentUuid;
            _config = new ConsumerConfig();
            config.GetSection("ConsumerConfig").Bind(_config);

            _config.EnableAutoCommit = false;
            _config.AutoOffsetReset = AutoOffsetResetType.Earliest;
            _config.GroupId = GroupId;

            this.Consumer = new Consumer<string, byte[]>(_config);
        }

        public void Dispose()
        {
            Consumer?.Dispose();
            //SubscriberThread.Abort();
        }

    }
}
