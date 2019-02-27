using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Serialization;
using System.Threading.Tasks;
using Grpc.Core;
using Orleans;
using Scynet.GrainInterfaces.Agent;
using Scynet.GrainInterfaces.Registry;
using Scynet.GrainInterfaces.Strategy;

namespace Scynet.Grains.Strategy
{
    [Serializable]
    public abstract class BasicAgentStrategyLogic : IAgentStrategyLogic
    {
        /// <inheritdoc/>
        public void GetObjectData(SerializationInfo info, StreamingContext context)
        {

        }

        /// <inheritdoc/>
        public abstract Task<bool> Apply(Guid id, AgentInfo agentInfo, AgentStrategyLogicContext context);

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
    public class BasicHibernateStrategy : BasicAgentStrategyLogic
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
    public class BasicSubscribeStrategy : BasicAgentStrategyLogic
    {
        /// <inheritdoc/>
        public override Task<bool> Apply(Guid id, AgentInfo agentInfo, AgentStrategyLogicContext context)
        {
            return Task.FromResult(true);
        }
    }

    [Serializable]
    public class BasicPublishStrategy : BasicAgentStrategyLogic
    {
        /// <inheritdoc/>
        public override Task<bool> Apply(Guid id, AgentInfo agentInfo, AgentStrategyLogicContext context)
        {
            return Task.FromResult(true); // TODO: Make some simple check about the score of the agent
        }
    }
}
