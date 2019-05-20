using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Channels;
using System.Threading.Tasks;
using Scynet.GrainInterfaces.Agent;
using Confluent.Kafka;
﻿using Microsoft.AspNetCore.SignalR;

namespace Scynet.HatcheryFacade.SignalRNotifications
{
    public interface INotifyHubClient
    {
        Task BroadcastNewAgent(Guid key, AgentInfo agentInfo);
    }

    public class NotifyHub : Hub<INotifyHubClient>
    {
        private readonly KafkaConsumerHelper _kafkaConsumerHelper;
        public NotifyHub(KafkaConsumerHelper kafkaConsumerHelper) {
            _kafkaConsumerHelper = kafkaConsumerHelper;
        }

        public ChannelReader<PriceData> GetPrices(Guid uuid, CancellationToken cancellationToken)
        {
            var channel = Channel.CreateUnbounded<PriceData>();
            var fromTime = DateTime.Now.Subtract(TimeSpan.FromDays(5));

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

        public ChannelReader<Prediction> GetPredictions(Guid uuid, CancellationToken cancellationToken)
        {
            Console.WriteLine("Wooot!");
            var channel = Channel.CreateUnbounded<Prediction>();
            var fromTime = DateTime.Now.Subtract(TimeSpan.FromDays(5));

            this._kafkaConsumerHelper.ConsumeStream(uuid.ToString() + "-evala", cancellationToken, (ConsumeResult<string, byte[]> res) =>
            {
                var blob = Blob.Parser.ParseFrom(res.Value);
                var dateTime = (new DateTime(1970, 1, 1)).AddSeconds(long.Parse(res.Key));

                if (dateTime > fromTime)
                {
                    channel.Writer.TryWrite(new Prediction
                    {
                        Date = dateTime.ToString(),
                        Value = blob.Data[0],
                        IsTrue = blob.Data[1] > 0.5
                    });
                }
            });

            return channel.Reader;
        }
    }
}
