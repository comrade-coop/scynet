using System;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace Scynet.GrainInterfaces
{
    /// <summary>
    /// An agent created by a component
    /// </summary>
    public interface IComponentAgent : IAgent
    {
        /// <summary>
        /// Initialize the agent
        /// </summary>
        Task Initialize(AgentInfo info, IEnumerable<IAgent> inputs, byte[] data);

        /// <summary>
        /// Get the component of the agent
        /// </summary>
        Task<IComponent> GetComponent();

        /// <summary>
        /// Get the data of the agent
        /// </summary>
        Task<byte[]> GetData();

        /// <summary>
        /// Get the inputs of the agent
        /// </summary>
        Task<IEnumerable<IAgent>> GetInputs();
    }
}
