using System;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace Scynet.GrainInterfaces
{
    /// <summary>
    /// A Strategy which decides what agents to output
    /// </summary>
    public interface IPublishStrategy : IStrategy, Orleans.IGrainWithGuidKey
    {
    }
}
