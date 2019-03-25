using System.Collections.Generic;
using System.Threading.Tasks;

namespace Scynet.GrainInterfaces.Component
{
    /// <summary>
    /// Base interface for users of agents
    /// </summary>
    public interface IEngager : Orleans.IGrainObserver
    {
        /// <summary>
        /// Notification that an engaged agent was released.
        /// </summary>
        /// <param name="agent">The agent which was released</param>
        void Released(IAgent agent);
    }
}
