using System;
using System.Threading.Tasks;

namespace Scynet.GrainInterfaces.Strategy
{
    /// <summary>
    /// A Strategy which decides what agents are suitable as input
    /// </summary>
    public interface ISubscribeStrategy : IStrategy, Orleans.IGrainWithGuidKey
    {
    }
}
