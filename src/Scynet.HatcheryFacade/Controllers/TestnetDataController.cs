using Confluent.Kafka;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.SignalR;
using Microsoft.Extensions.Logging;
using Scynet.HatcheryFacade.CustomKafkaConsumer;
using Scynet.HatcheryFacade.SignalRNotifications;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;

namespace Scynet.HatcheryFacade.Controllers
{
    [Route("api/data")]
    [ApiController]
    public class TestnetDataController : ControllerBase
    {
        private readonly ILogger<TestnetDataController> _logger;
        private readonly IHubContext<NotifyHub, IHubClient> _hubContext;
        private readonly KafkaConsumerFacade _kafkaConsumerFacade;

        public TestnetDataController(ILogger<TestnetDataController> logger,
            IHubContext<NotifyHub, IHubClient> hubContext, KafkaConsumerFacade kafkaConsumerFacade)
        {
            _logger = logger;
            _hubContext = hubContext;
            _kafkaConsumerFacade = kafkaConsumerFacade;
        }

        [Route("predictions")]
        public void SubPredictions(string uuid)
        {
            this._kafkaConsumerFacade.StartConsuming(uuid, (ConsumeResult<string, byte[]> res) =>
            {
                var str = System.Text.Encoding.Default.GetString(res.Value);
                try
                {
                    var x = Blob.Parser.ParseFrom(res.Value);
                    var num = long.Parse(res.Key) * 1000;
                    var dt = (new DateTime(1970, 1, 1)).AddMilliseconds(num);

                    var prediction = new Prediction
                    {
                        Date = dt.ToString(),
                        Value = x.Data[0]
                    };
                    this._hubContext.Clients.All.BroadcastAgentPredictions(prediction);
                }
                catch (Exception e)
                {
                    Console.WriteLine(e);
                }

            });
        }

        [Route("ethprice")]
        public void GetETHPrices(string uuid)
        {
            this._kafkaConsumerFacade.StartConsuming(uuid, (ConsumeResult<string, byte[]> res) =>
            {
                try
                {
                    var x = Blob.Parser.ParseFrom(res.Value);
                    var num = long.Parse(res.Key) * 1000;
                    var dt = (new DateTime(1970, 1, 1)).AddMilliseconds(num);
                    var priceData = new PriceData
                    {
                        Date = dt.ToString(),
                        Close = x.Data[0],
                        High = x.Data[1],
                        Low = x.Data[2],
                        Open = x.Data[3],
                        VolumeFrom = x.Data[4],
                        VolumeTo = x.Data[5],
                    };
                    this._hubContext.Clients.All.BroadcastETHPrice(priceData);
                }
                catch (Exception e)
                {
                    Console.WriteLine(e);
                }
            });
        }

        [Route("unsubscribe")]
        public void Unsubscribe(string uuid)
        {
            this._kafkaConsumerFacade.StopConsuming(uuid);
        }
    }
}
