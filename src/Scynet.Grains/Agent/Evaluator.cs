using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Confluent.Kafka;
using Google.Protobuf;
using Microsoft.Extensions.Logging;
using Orleans;
using Orleans.Providers;
using Scynet.GrainInterfaces.Agent;
using Scynet.GrainInterfaces.Registry;

namespace Scynet.Grains.Agent
{
    public static class IConsumerExtensions {
        public static Task<ConsumeResult<K, V>> ConsumeAsync<K, V>(this IConsumer<K, V> consumer) {
            return Task.Run(() => consumer.Consume());
        }
    }

    public class EvaluatorState
    {
        // [prediction, target] => count
        public int[,] ResultsMatrix = new int[2,2] { {0, 0}, {0, 0} };
        public IAgent TargetAgent;
    }

    public interface IEvaluator : IGrainWithGuidKey
    {
        Task Start(IAgent target);
    }

    public class Evaluator : Grain<EvaluatorState>, IEngager, IEvaluator
    {
        const string TargetsStream = "331d591b-184d-4e7c-b075-9841181c05c1";
        private IConsumer<string, byte[]> PredictionConsumer;
        private IConsumer<string, byte[]> TargetConsumer;
        private IProducer<string, byte[]> ResultProducer;
        private string ResultStream;
        private bool Running = false;

        public Task Start(IAgent target) {
            if (!Running) {
                State.TargetAgent = target;
                Init();
                Run();
                Running = true;
                return base.WriteStateAsync();
            } else {
                return Task.CompletedTask;
            }
        }

        private void Init() {

            string agentId = State.TargetAgent.GetPrimaryKey().ToString();
            var consumerConfig = new ConsumerConfig() {
                GroupId = "evaluatorsala-" + agentId,
                BootstrapServers = "127.0.0.1:9092",
                AutoOffsetReset = AutoOffsetReset.Earliest
            };
            var producerConfig = new ProducerConfig() {
                BootstrapServers = "127.0.0.1:9092"
            };
            PredictionConsumer = new ConsumerBuilder<string, byte[]>(consumerConfig).Build();
            PredictionConsumer.Subscribe(agentId);
            if (agentId == TargetsStream) {
                consumerConfig = new ConsumerConfig() {
                    GroupId = "evaluatorsala--" + agentId,
                    BootstrapServers = "127.0.0.1:9092",
                    AutoOffsetReset = AutoOffsetReset.Earliest
                };
            }
            TargetConsumer = new ConsumerBuilder<string, byte[]>(consumerConfig).Build();
            TargetConsumer.Subscribe(TargetsStream);
            ResultProducer = new ProducerBuilder<string, byte[]>(producerConfig).Build();
            ResultStream = agentId + "-evala";
        }

        private async void Run()
        {
            try {
                await State.TargetAgent.Engage(this);
                while (true) {
                    ConsumeResult<string, byte[]> predictionMessage = await PredictionConsumer.ConsumeAsync();
                    ConsumeResult<string, byte[]> targetMessage = await TargetConsumer.ConsumeAsync();

                    while (true) {
                        var predictionKey = long.Parse(predictionMessage.Key);
                        var targetKey = long.Parse(targetMessage.Key);
                        if (predictionKey < targetKey) {
                            predictionMessage = await PredictionConsumer.ConsumeAsync();
                        } else if (predictionKey > targetKey) {
                            targetMessage = await TargetConsumer.ConsumeAsync();
                        } else {
                            break;
                        }

                    }

                    var PredictionBlob = Blob.Parser.ParseFrom(predictionMessage.Value);
                    var TargetBlob = Blob.Parser.ParseFrom(targetMessage.Value);

                    if (PredictionBlob.Data.Count >= 1 && TargetBlob.Data.Count >= 1) {
                        var i = PredictionBlob.Data[0] > 0.5 ? 1 : 0;
                        var j = TargetBlob.Data[0] > 0.5 ? 1 : 0;
                        State.ResultsMatrix[i,j] += 1;
                        await ResultProducer.ProduceAsync(ResultStream, new Message<string, byte[]> {
                            Key = predictionMessage.Key,
                            Value = (new Blob() {
                                Shape = new Shape() {
                                    Dimension = {2}
                                },
                                Data = {PredictionBlob.Data[0], (i == j ? 1 : 0)}
                            }).ToByteArray()
                        });
                        await base.WriteStateAsync();
                        // no awaiting here, best-effort
                        var TotalPositive = (double)State.ResultsMatrix[1,1] + State.ResultsMatrix[1,0];
                        var TotalNegative = (double)State.ResultsMatrix[0,1] + State.ResultsMatrix[0,0];
                        State.TargetAgent.SetMetadata("TruePositive", (State.ResultsMatrix[1,1] / TotalPositive).ToString());
                        State.TargetAgent.SetMetadata("TrueNegative", (State.ResultsMatrix[0,0] / TotalNegative).ToString());
                        State.TargetAgent.SetMetadata("FalsePositive", (State.ResultsMatrix[0,1] / TotalPositive).ToString());
                        State.TargetAgent.SetMetadata("FalseNegative", (State.ResultsMatrix[1,0] / TotalNegative).ToString());
                    }

                }
            }
            catch (Exception e) {
                Running = false;
                Console.WriteLine(e);
            }
        }

        public void Released(IAgent agent) {
            // pass
        }
    }
}
