using Microsoft.Extensions.Logging;
using Scynet.GrainInterfaces;
using Serialize.Linq.Nodes;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Linq.Expressions;
using System.Text;
using System.Threading.Tasks;

namespace Scynet.Grains
{
    public class RegistryState<Т>
    {
        public List<Т> Items = new List<Т>();
    }

    public abstract class Registry<T> : Orleans.Grain<RegistryState<T>>, IRegistry<T>
    {
        private readonly ILogger<Registry<T>> Logger;

        public Registry(ILogger<Registry<T>> logger)
        {
            Logger = logger;
        }

        public Task Register(T info)
        {
            Logger.LogInformation($"Item registered ({info})!");
            State.Items.Add(info);
            return base.WriteStateAsync();
        }

        public Task<K> Query<K>(ExpressionNode expression)
        {
            var compiledExpression = expression.ToExpression<Func<IEnumerable<T>, K>>().Compile();
            K result = compiledExpression(State.Items);
            return Task.FromResult(result);
        }
    }

    // HACK: Needed so that Orleans can find the Grain types
    public class AgentRegistry : Registry<AgentInfo>
    {
        public AgentRegistry(ILogger<AgentRegistry> logger) : base(logger) {}
    }
    public class ComponentRegistry : Registry<ComponentInfo>
    {
        public ComponentRegistry(ILogger<ComponentRegistry> logger) : base(logger) {}
    }
}
