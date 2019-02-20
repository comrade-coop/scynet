using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Runtime.Serialization;
using Orleans;
using Scynet.GrainInterfaces;

namespace Scynet.Grains
{
    /// <summary>
    /// Context which contains additional information that can be used by the strategy
    /// </summary>
    public class AgentStrategyLogicContext
    {
        public IGrainFactory GrainFactory { get; }

        public AgentStrategyLogicContext(IGrainFactory grainFactory)
        {
            GrainFactory = grainFactory;
        }
    }

    /// <summary>
    /// Base interface which decides what agents are fit for hibernating/publishing/subscribing
    /// </summary>
    public interface IAgentStrategyLogic : ISerializable
    {
        /// <summary>
        /// Apply the strategy to an agent.
        /// </summary>
        Task<bool> Apply(Guid id, AgentInfo agent, AgentStrategyLogicContext context);

        /// <summary>
        /// Set the source of the strategy as a string.
        /// </summary>
        Task SetSource(String source);

        /// <summary>
        /// Get the source of the strategy as a string.
        /// </summary>
        Task<String> GetSource();
    }
}
