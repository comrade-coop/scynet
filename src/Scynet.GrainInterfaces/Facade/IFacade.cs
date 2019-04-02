using System.Collections.Generic;
using System.Threading.Tasks;
using Scynet.GrainInterfaces.Agent;

namespace Scynet.GrainInterfaces.Facade
{
    /// <summary>
    /// Interface for facades.
    /// </summary>
    public interface IFacade : Orleans.IGrainObserver
    {
        /// <summary>
        /// Called to start streaming an external agent
        /// </summary>
        /// <param name="agent">The agent to stream</param>
        void Start(IExternalAgent agent);

        /// <summary>
        /// Called to stop streaming an external agent
        /// </summary>
        /// <param name="agent">The agent to stream</param>
        void Stop(IExternalAgent agent);
    }
}
