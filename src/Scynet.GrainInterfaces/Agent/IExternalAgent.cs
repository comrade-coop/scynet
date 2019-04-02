using System;
using System.Threading.Tasks;

namespace Scynet.GrainInterfaces.Agent
{
    /// <summary>
    /// An agent created by a component
    /// </summary>
    public interface IExternalAgent : IAgent
    {
        /// <summary>
        /// Initialize the agent
        /// </summary>
        Task Initialize(AgentInfo info, String address);

        /// <summary>
        /// Get the component of the agent
        /// </summary>
        Task<String> GetAddress();
    }
}
