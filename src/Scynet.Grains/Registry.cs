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
    public class RegistryState<T>
    {
        public List<T> Items = new List<T>();
        // TODO: Can the below line be shortened somehow?
        public Dictionary<Tuple<IRegistryListener<T>, String>, ExpressionNode> Subscriptions =
            new Dictionary<Tuple<IRegistryListener<T>, String>, ExpressionNode>();
    }

    public abstract class Registry<T> : Orleans.Grain<RegistryState<T>>, IRegistry<T>
    {
        private readonly ILogger<Registry<T>> Logger;
        private Dictionary<Tuple<IRegistryListener<T>, String>, Func<T, bool>> SubscriptionFilterCache =
            new Dictionary<Tuple<IRegistryListener<T>, String>, Func<T, bool>>();

        public Registry(ILogger<Registry<T>> logger)
        {
            Logger = logger;
        }

        public Task Register(T info)
        {
            Logger.LogInformation($"Item registered ({info})!");
            State.Items.Add(info);
            foreach (var subscription in State.Subscriptions) {
                if (!SubscriptionFilterCache.ContainsKey(subscription.Key)) {
                    SubscriptionFilterCache[subscription.Key] =
                        subscription.Value.ToExpression<Func<T, bool>>().Compile();
                }
                var filter = SubscriptionFilterCache[subscription.Key];
                if (filter(info)) {
                    var listener = subscription.Key.Item1;
                    var @ref = subscription.Key.Item2;
                    listener.NewItem(@ref, info);
                }
            }
            return base.WriteStateAsync();
        }

        public Task<K> QueryValue<K>(ExpressionNode expression)
        {
            var compiledExpression = expression.ToExpression<Func<IEnumerable<T>, K>>().Compile();
            K result = compiledExpression(State.Items);
            return Task.FromResult(result);
        }

        public Task<IEnumerable<K>> QueryCollection<K>(ExpressionNode expression)
        {
            var compiledExpression = expression.ToExpression<Func<IEnumerable<T>, IEnumerable<K>>>().Compile();
            IEnumerable<K> result = compiledExpression(State.Items).ToList();
            return Task.FromResult(result);
        }

        public Task Subscribe(ExpressionNode expression, IRegistryListener<T> listener, String @ref = "")
        {
            var key = Tuple.Create(listener, @ref);
            SubscriptionFilterCache.Remove(key);
            State.Subscriptions[key] = expression;
            return base.WriteStateAsync();
        }

        public Task Unsubscribe(IRegistryListener<T> listener, String @ref = "")
        {
            var key = Tuple.Create(listener, @ref);
            State.Subscriptions.Remove(key);
            SubscriptionFilterCache.Remove(key);
            return base.WriteStateAsync();
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
