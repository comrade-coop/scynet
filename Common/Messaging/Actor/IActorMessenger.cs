using System.Threading.Tasks;
using Microsoft.ServiceFabric.Actors;

namespace Common.Messaging.Actor
{
    public interface IActorMessenger : IActor
    {
        Task AddSubscription(string topic, SubscriberReference subscriber);
        Task RemoveSubscription(string topic, SubscriberReference subscriber);
        Task ReceiveMessage(SubscriptionMessage message);
    }
}
