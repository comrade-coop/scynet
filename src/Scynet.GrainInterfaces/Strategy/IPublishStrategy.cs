using System;
using System.Threading.Tasks;

namespace Scynet.GrainInterfaces.Strategy
{
    /// <summary>
    /// A Strategy which decides what agents to output
    /// </summary>
    public interface IPublishStrategy : IStrategy, Orleans.IGrainWithGuidKey
    {
    }
}
