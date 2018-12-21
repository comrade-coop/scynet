using System.Threading.Tasks;
using Microsoft.ServiceFabric.Services.Remoting;

namespace Common.Messaging.Service
{
    public interface IServiceMessenger : IService
    {
        Task AddSubscription(string topic, SubscriberReference subscriber);
        Task RemoveSubscription(string topic, SubscriberReference subscriber);
        Task ReceiveMessage(SubscriptionMessage message);
    }
}
