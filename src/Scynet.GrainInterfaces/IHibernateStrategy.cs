using System;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace Scynet.GrainInterfaces
{
    /// <summary>
    /// A Strategy which decides when to hibernate agents
    /// </summary>
    public interface IHibernateStrategy : IStrategy, Orleans.IGrainWithGuidKey
    {
    }
}
