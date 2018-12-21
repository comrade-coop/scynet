using Common.Messaging.Service;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Common.Topics.Service
{
    public interface ITopicWriterService : IServiceMessenger
    {
        Task Write(string topic, long key, Byte[] data);
    }
}
