using System.Threading.Tasks;
using Microsoft.ServiceFabric.Actors;

namespace Common.Messaging.Actor
{
    public interface IActorMessenger : IActor
    {
        Task AddSubscription(string channel, SubscriberReference subscriber);
        Task RemoveSubscription(string channel, SubscriberReference subscriber);
        Task ReceiveMessage(SubscriptionMessage message);
    }
}
