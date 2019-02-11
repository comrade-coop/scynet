using System;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace Scynet.GrainInterfaces
{
    /// <summary>
    /// An agent created by a component
    /// </summary>
    public interface IExternalAgent : IAgent
    {
        /// <summary>
        /// Initialize the agent
        /// </summary>
        Task Initialize(string address);

        /// <summary>
        /// Get the component of the agent
        /// </summary>
        Task<string> GetAddress();
    }
}
