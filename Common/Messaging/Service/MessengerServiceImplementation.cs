using System;
using System.Diagnostics.Tracing;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.ServiceFabric.Data;
using Microsoft.ServiceFabric.Data.Collections;

namespace Common.Messaging.Service
{
    public class MessengerServiceImplementation : IMessengerImplementation
    {
        private const string PublishQueue = "PublishQueue";
        private const string ReceiveQueue = "ReceiveQueue";
        private const string SubscribersDictionary = "SubscribersDictionary";

        private readonly IReliableStateManager stateManager;

        public MessengerServiceImplementation(IReliableStateManager stateManager)
        {
            this.stateManager = stateManager;
        }

        public async Task AddSubscription(string channel, SubscriberReference subscriber)
        {
            using (ITransaction tx = stateManager.CreateTransaction())
            {
                var subscriberKey = subscriber.Id;
                var subscribers =
                    await stateManager.GetOrAddAsync<IReliableDictionary<string, SubscriberReference>>(
                        SubscribersDictionary + channel);

                await subscribers.AddOrUpdateAsync(tx, subscriberKey, subscriber, (key, reference) => subscriber);
                await tx.CommitAsync();
            }
        }

        public async Task<SubscriptionMessage> ProcessMessage(Action<SubscriptionMessage> action = null)
        {
            SubscriptionMessage result = null;
            using (ITransaction tx = stateManager.CreateTransaction())
            {
                var receiveQueue = await stateManager.TryGetAsync<IReliableQueue<SubscriptionMessage>>(ReceiveQueue);

                if (receiveQueue.HasValue && (await receiveQueue.Value.GetCountAsync(tx)) > 0)
                {
                    var dequeuedMessageResult = await receiveQueue.Value.TryDequeueAsync(tx);
                    result = dequeuedMessageResult.Value;

                    action?.Invoke(result);
                }

                await tx.CommitAsync();
            }

            return result;
        }

        public async Task PublishMessage(SubscriptionMessage message)
        {
            using (ITransaction tx = stateManager.CreateTransaction())
            {
                var publishQueue = await stateManager.GetOrAddAsync<IReliableQueue<SubscriptionMessage>>(PublishQueue);
                await publishQueue.EnqueueAsync(tx, message);
                await tx.CommitAsync();
            }

            //This should probably be split out similarly to how ReceiveMessage & ProcessMessage work
            using (ITransaction tx = stateManager.CreateTransaction())
            {
                var publishQueue = await stateManager.TryGetAsync<IReliableQueue<SubscriptionMessage>>(PublishQueue);

                if (publishQueue.HasValue)
                {
                    var queuedMessage = await publishQueue.Value.TryDequeueAsync(tx);
                    var subscribers =
                        await stateManager.TryGetAsync<IReliableDictionary<string, SubscriberReference>>(SubscribersDictionary + queuedMessage.Value.Channel);

                    if (subscribers.HasValue)
                    {
                        var enumerator =
                            (await subscribers.Value.CreateEnumerableAsync(tx, EnumerationMode.Ordered)).GetAsyncEnumerator();

                        while (await enumerator.MoveNextAsync(CancellationToken.None))
                        {
                            SubscriberReference subscriber = enumerator.Current.Value;
                            await subscriber.Trigger(queuedMessage.Value);
                        }
                    }
                }


                await tx.CommitAsync();
            }
        }

        public async Task ReceiveMessage(SubscriptionMessage message)
        {
            using (ITransaction tx = stateManager.CreateTransaction())
            {
                var receiveQueue = await stateManager.GetOrAddAsync<IReliableQueue<SubscriptionMessage>>(ReceiveQueue);
                await receiveQueue.EnqueueAsync(tx, message);
                await tx.CommitAsync();
            }
        }

        public async Task RemoveSubscription(string channel, SubscriberReference subscriber)
        {
            using (ITransaction tx = stateManager.CreateTransaction())
            {
                var subscriberKey = subscriber.Id;
                var subscribers =
                    await stateManager.TryGetAsync<IReliableDictionary<string, SubscriberReference>>(SubscribersDictionary + channel);

                if (subscribers.HasValue)
                {
                    await subscribers.Value.TryRemoveAsync(tx, subscriberKey);
                }

                await tx.CommitAsync();
            }
        }
    }
}
