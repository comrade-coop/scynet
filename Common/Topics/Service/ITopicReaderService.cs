using Common.Messaging.Service;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Common.Topics.Service
{
    public interface ITopicReaderService : IServiceMessenger
    {
        Task<Byte[]> ReadKey(string topic, long key);
        Task<List<Byte[]>> ReadKeyRange(string topic, long start, long end);
        Task<List<Byte[]>> ReadLatest(string topic);
        Task<long> GetLastReadKey(string topic);        
    }
}
