using Confluent.Kafka;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.SignalR;
using Microsoft.Extensions.Logging;
using Scynet.HatcheryFacade.CustomKafkaConsumer;
using Scynet.HatcheryFacade.SignalRNotifications;
using System;
using System.Collections.Generic;
using System.Linq;
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
            this._kafkaConsumerFacade.StartConsuming(uuid, (byte[] res) =>
            {
                var str = System.Text.Encoding.Default.GetString(res);
                try
                {
                    var x = Blob.Parser.ParseFrom(res);
                    this._hubContext.Clients.All.BroadcastAgentPredictions(x);
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
            this._kafkaConsumerFacade.StartConsuming(uuid, (byte[] res) =>
            {
                try
                {
                    var x = Blob.Parser.ParseFrom(res);
                    this._hubContext.Clients.All.BroadcastETHPrice(x);
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
