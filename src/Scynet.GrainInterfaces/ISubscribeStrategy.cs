using System;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace Scynet.GrainInterfaces
{
    /// <summary>
    /// A Strategy which decides what agents are suitable as input
    /// </summary>
    public interface ISubscribeStrategy : IStrategy, Orleans.IGrainWithGuidKey
    {
    }
}
