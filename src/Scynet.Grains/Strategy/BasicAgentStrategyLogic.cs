using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Serialization;
using System.Threading.Tasks;
using Grpc.Core;
using Orleans;
using Scynet.GrainInterfaces.Component;
using Scynet.GrainInterfaces.Registry;
using Scynet.GrainInterfaces.Strategy;

namespace Scynet.Grains.Strategy
{
    [Serializable]
    public abstract class BasicAgentStrategyLogic<T> : IAgentStrategyLogic<T>
    {
        /// <inheritdoc/>
        public void GetObjectData(SerializationInfo info, StreamingContext context) { }

        /// <inheritdoc/>
        public abstract Task<T> Apply(Guid id, AgentInfo agentInfo, AgentStrategyLogicContext context);

        /// <inheritdoc/>
        public Task SetSource(String source)
        {
            return Task.CompletedTask;
        }

        /// <inheritdoc/>
        public Task<String> GetSource()
        {
            return Task.FromResult("");
        }
    }

    [Serializable]
    public class BasicHibernateStrategy : BasicAgentStrategyLogic<bool>
    {
        /// <inheritdoc/>
        public override async Task<bool> Apply(Guid id, AgentInfo agentInfo, AgentStrategyLogicContext context)
        {
            var agent = context.GrainFactory.GetGrain<IAgent>(id);
            var engagements = await agent.GetActiveEngagements();
            return engagements.Count() > 0;
        }
    }

    [Serializable]
    public class BasicInputOutputStrategy : BasicAgentStrategyLogic<bool>
    {
        /// <inheritdoc/>
        public override Task<bool> Apply(Guid id, AgentInfo agentInfo, AgentStrategyLogicContext context)
        {
            return Task.FromResult(true); // TODO: Make some simple check about the score of the agent
        }
    }

    [Serializable]
    public class BasicPricingStrategy : BasicAgentStrategyLogic<uint>
    {
        /// <inheritdoc/>
        public override Task<uint> Apply(Guid id, AgentInfo agentInfo, AgentStrategyLogicContext context)
        {
            return Task.FromResult(agentInfo.Price + 1);
        }
    }
}
