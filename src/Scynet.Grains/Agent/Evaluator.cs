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
        public static Task<ConsumeResult<K, V>> ConsumeAsync<K, V>(this IConsumer<K, V> consumer, TimeSpan timeout) {
            return Task.Run(() => consumer.Consume(timeout));
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
                GroupId = "evaluator-data-" + agentId,
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
                    GroupId = "evaluator-target-" + agentId,
                    BootstrapServers = "127.0.0.1:9092",
                    AutoOffsetReset = AutoOffsetReset.Earliest
                };
            }
            TargetConsumer = new ConsumerBuilder<string, byte[]>(consumerConfig).Build();
            TargetConsumer.Subscribe(TargetsStream);
            ResultProducer = new ProducerBuilder<string, byte[]>(producerConfig).Build();
            ResultStream = agentId + "-evaluated";
        }

        private async void Run()
        {
            Console.WriteLine("Evaluator now running!");
            try {
                // await State.TargetAgent.Engage(this);
                while (true) {
                    ConsumeResult<string, byte[]> predictionMessage = await PredictionConsumer.ConsumeAsync();
                    ConsumeResult<string, byte[]> targetMessage = await TargetConsumer.ConsumeAsync(TimeSpan.FromSeconds(10));

                    if (targetMessage == null)
                    {
                        // no awaiting here, best-effort
                        var TotalPositive = (double)State.ResultsMatrix[1,1] + State.ResultsMatrix[0,1];
                        var TotalNegative = (double)State.ResultsMatrix[1,0] + State.ResultsMatrix[0,0];
                        State.TargetAgent.SetMetadata("TruePositive", (State.ResultsMatrix[1,1] / TotalPositive).ToString());
                        State.TargetAgent.SetMetadata("TrueNegative", (State.ResultsMatrix[0,0] / TotalNegative).ToString());
                        State.TargetAgent.SetMetadata("FalsePositive", (State.ResultsMatrix[1,0] / TotalNegative).ToString());
                        State.TargetAgent.SetMetadata("FalseNegative", (State.ResultsMatrix[0,1] / TotalPositive).ToString());
                        do {
                            var predictionBlob = Blob.Parser.ParseFrom(predictionMessage.Value);
                            await ResultProducer.ProduceAsync(ResultStream, new Message<string, byte[]> {
                                Key = predictionMessage.Key,
                                Value = (new Blob() {
                                    Shape = new Shape() {
                                        Dimension = {2}
                                    },
                                    Data = {predictionBlob.Data[0], -1}
                                }).ToByteArray()
                            });
                            predictionMessage = await PredictionConsumer.ConsumeAsync(TimeSpan.FromSeconds(2));
                        } while (predictionMessage != null);
                    } else {

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

                        var predictionBlob = Blob.Parser.ParseFrom(predictionMessage.Value);
                        var targetBlob = Blob.Parser.ParseFrom(targetMessage.Value);

                        if (predictionBlob.Data.Count >= 1 && targetBlob.Data.Count >= 1) {
                            var i = predictionBlob.Data[0] > 0.5 ? 1 : 0;
                            var j = targetBlob.Data[0] > 0.5 ? 1 : 0;
                            State.ResultsMatrix[i,j] += 1;
                            await ResultProducer.ProduceAsync(ResultStream, new Message<string, byte[]> {
                                Key = predictionMessage.Key,
                                Value = (new Blob() {
                                    Shape = new Shape() {
                                        Dimension = {2}
                                    },
                                    Data = {predictionBlob.Data[0], (i == j ? 1 : 0)}
                                }).ToByteArray()
                            });
                            await base.WriteStateAsync();
                        }
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
