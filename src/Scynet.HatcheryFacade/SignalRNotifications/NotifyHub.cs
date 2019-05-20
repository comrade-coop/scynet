using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Channels;
using System.Threading.Tasks;
using Scynet.GrainInterfaces.Agent;
using Scynet.GrainInterfaces.Registry;
using Confluent.Kafka;
﻿using Microsoft.AspNetCore.SignalR;
using Orleans;

namespace Scynet.HatcheryFacade.SignalRNotifications
{
    public interface INotifyHubClient
    {
        Task BroadcastNewAgent(Guid key, AgentInfo agentInfo);
    }

    public class NotifyHub : Hub<INotifyHubClient>
    {
        private class Engager : IEngager
        {
            public void Released(IAgent agent) {} // No problem!
        };

        private readonly KafkaConsumerHelper _kafkaConsumerHelper;
        private readonly IClusterClient _clusterClient;
        public NotifyHub(KafkaConsumerHelper kafkaConsumerHelper, IClusterClient clusterClient) {
            _kafkaConsumerHelper = kafkaConsumerHelper;
            _clusterClient = clusterClient;
        }

        public async Task<ChannelReader<PriceData>> GetPrices(Guid uuid, CancellationToken cancellationToken)
        {
            var channel = Channel.CreateUnbounded<PriceData>();
            var fromTime = DateTime.Now.Subtract(TimeSpan.FromDays(5));

            var engager = await _clusterClient.CreateObjectReference<IEngager>(new Engager());
            var registry = _clusterClient.GetGrain<IRegistry<Guid, AgentInfo>>(0);
            var agent = (await registry.Get(uuid)).Agent;
            await agent.Engage(engager);

            this._kafkaConsumerHelper.ConsumeStream(uuid.ToString(), cancellationToken, (ConsumeResult<string, byte[]> res) =>
            {
                var blob = Blob.Parser.ParseFrom(res.Value);
                var dateTime = (new DateTime(1970, 1, 1)).AddSeconds(long.Parse(res.Key));

                if (dateTime > fromTime)
                {
                    channel.Writer.TryWrite(new PriceData
                    {
                        Date = dateTime.ToString(),
                        Close = blob.Data[0],
                        High = blob.Data[1],
                        Low = blob.Data[2],
                        Open = blob.Data[3],
                        VolumeFrom = blob.Data[4],
                        VolumeTo = blob.Data[5],
                    });
                }
            });

            return channel.Reader;
        }

        public async Task<ChannelReader<Prediction>> GetPredictions(Guid uuid, CancellationToken cancellationToken)
        {
            var channel = Channel.CreateUnbounded<Prediction>();
            var fromTime = DateTime.Now.Subtract(TimeSpan.FromDays(5));

            var engager = await _clusterClient.CreateObjectReference<IEngager>(new Engager());
            var registry = _clusterClient.GetGrain<IRegistry<Guid, AgentInfo>>(0);
            var agent = (await registry.Get(uuid)).Agent;
            await agent.Engage(engager);


            this._kafkaConsumerHelper.ConsumeStream(uuid.ToString() + "-evaluated", cancellationToken, (ConsumeResult<string, byte[]> res) =>
            {
                var blob = Blob.Parser.ParseFrom(res.Value);
                var dateTime = (new DateTime(1970, 1, 1)).AddSeconds(long.Parse(res.Key));

                if (dateTime > fromTime)
                {
                    channel.Writer.TryWrite(new Prediction
                    {
                        Date = dateTime.ToString(),
                        Value = blob.Data[0],
                        IsTrue = blob.Data[1] > 0 ? blob.Data[1] > 0.5 : (bool?)null
                    });
                }
            });

            return channel.Reader;
        }
    }
}
