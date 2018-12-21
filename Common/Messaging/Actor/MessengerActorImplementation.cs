using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using Microsoft.ServiceFabric.Actors.Runtime;

namespace Common.Messaging.Actor
{
    public class MessengerActorImplementation : IMessengerImplementation
    {
        private const string SubscribersDictionary = "SubscribersDictionary";
        private const string PublishQueue = "PublishQueue";
        private const string ReceiveQueue = "ReceiveQueue";

        private readonly IActorStateManager stateManager;

        public MessengerActorImplementation(IActorStateManager stateManager)
        {
            this.stateManager = stateManager;
        }

        //TODO: implement channel support
        public async Task AddSubscription(string channel, SubscriberReference subscriber)
        {
            var subscribers = await stateManager.GetOrAddStateAsync(SubscribersDictionary + channel,
                new Dictionary<string, SubscriberReference>());

            var subscriberKey = subscriber.Id;
            subscribers.Add(subscriberKey, subscriber);
            await stateManager.AddOrUpdateStateAsync(SubscribersDictionary + channel, subscribers, (key, value) => subscribers);
        }

        public async Task<SubscriptionMessage> ProcessMessage(Action<SubscriptionMessage> action = null)
        {
            SubscriptionMessage result = null;
            var receiveQueue = await stateManager.TryGetStateAsync<Queue<SubscriptionMessage>>(ReceiveQueue);

            if (receiveQueue.HasValue && receiveQueue.Value.Count > 0)
            {
                if (action != null)
                {
                    var messageResult = receiveQueue.Value.Peek();
                    action(messageResult);
                }

                result = receiveQueue.Value.Dequeue();
            }

            return result;
        }

        public async Task PublishMessage(SubscriptionMessage message)
        {
            var publishQueue = await stateManager.GetOrAddStateAsync(PublishQueue, new Queue<SubscriptionMessage>());
            publishQueue.Enqueue(message);

            //This should probably be split out similarly to how ReceiveMessage & ProcessMessage work
            if (publishQueue.Count > 0)
            {
                var queuedMessage = publishQueue.Dequeue();
                var subscribersDictionary =
                    await stateManager.TryGetStateAsync<Dictionary<string, SubscriberReference>>(SubscribersDictionary + message.Channel);

                if (subscribersDictionary.HasValue)
                {
                    foreach (KeyValuePair<string, SubscriberReference> subscriberPair in subscribersDictionary.Value)
                    {
                        await subscriberPair.Value.Trigger(queuedMessage);
                    }
                }
            }
        }

        public async Task ReceiveMessage(SubscriptionMessage message)
        {
            var receiveQueue = await stateManager.GetOrAddStateAsync(ReceiveQueue, new Queue<SubscriptionMessage>());
            receiveQueue.Enqueue(message);
        }

        public async Task RemoveSubscription(string channel, SubscriberReference subscriber)
        {

            var subscriberKey = subscriber.Id;
            var subscribersDictionary =
                await stateManager.TryGetStateAsync<Dictionary<string, SubscriberReference>>(SubscribersDictionary + channel);

            if (subscribersDictionary.HasValue)
            {
                subscribersDictionary.Value.Remove(subscriberKey);
            }
        }
    }
}
