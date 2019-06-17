using System;
using System.Collections.Generic;
using System.Linq;
using System.Linq.Expressions;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using Serialize.Linq.Nodes;
using Scynet.GrainInterfaces.Registry;
using Orleans.Providers;

namespace Scynet.Grains.Registry
{
    public class RegistryState<K, T>
    {
        public Dictionary<K, T> Items = new Dictionary<K, T>();
        // TODO: Can the below line be shortened somehow?
        public Dictionary<Tuple<IRegistryListener<K, T>, String>, Tuple<ExpressionNode, DateTime?>> Subscriptions =
            new Dictionary<Tuple<IRegistryListener<K, T>, String>, Tuple<ExpressionNode, DateTime?>>();
    }

    public abstract class Registry<K, T> : Orleans.Grain<RegistryState<K, T>>, IRegistry<K, T>
    {
        private readonly ILogger<Registry<K, T>> Logger;
        private Dictionary<Tuple<IRegistryListener<K, T>, String>, Func<K, T, bool>> SubscriptionFilterCache =
            new Dictionary<Tuple<IRegistryListener<K, T>, String>, Func<K, T, bool>>();

        public Registry(ILogger<Registry<K, T>> logger)
        {
            Logger = logger;
        }

        public Task Register(K key, T info)
        {
            Logger.LogInformation($"Item registered ({info})!");
            State.Items[key] = info;

            var staleSubsctiprions = new List<Tuple<IRegistryListener<K, T>, String>>();
            foreach (var subscription in State.Subscriptions)
            {
                if (subscription.Value.Item2 != null && DateTime.Now > subscription.Value.Item2)
                {
                    staleSubsctiprions.Add(subscription.Key);
                    continue;
                }

                if (!SubscriptionFilterCache.ContainsKey(subscription.Key))
                {
                    SubscriptionFilterCache[subscription.Key] =
                        subscription.Value.Item1.ToExpression<Func<K, T, bool>>().Compile();
                }

                var filter = SubscriptionFilterCache[subscription.Key];
                if (filter(key, info))
                {
                    var listener = subscription.Key.Item1;
                    var @ref = subscription.Key.Item2;
                    listener.NewItem(@ref, key, info);
                }
            }

            foreach (var staleKey in staleSubsctiprions)
            {
                State.Subscriptions.Remove(staleKey);
                SubscriptionFilterCache.Remove(staleKey);
            }

            return base.WriteStateAsync();
        }

        public Task<T> Get(K key)
        {
            return Task.FromResult(State.Items[key]);
        }

        public Task<U> QueryValue<U>(ExpressionNode expression)
        {
            var compiledExpression = expression.ToExpression<Func<IEnumerable<KeyValuePair<K, T>>, U>>().Compile();
            U result = compiledExpression(State.Items);
            return Task.FromResult(result);
        }

        public Task<IEnumerable<U>> QueryCollection<U>(ExpressionNode expression)
        {
            var compiledExpression = expression.ToExpression<Func<IEnumerable<KeyValuePair<K, T>>, IEnumerable<U>>>().Compile();
            IEnumerable<U> result = compiledExpression(State.Items).ToList();
            return Task.FromResult(result);
        }

        public Task Subscribe(ExpressionNode expression, IRegistryListener<K, T> listener, String @ref = "", TimeSpan? timeout = null)
        {
            var key = Tuple.Create(listener, @ref);
            SubscriptionFilterCache.Remove(key);
            State.Subscriptions[key] = Tuple.Create(expression, timeout != null ? DateTime.Now + timeout : null);
            return base.WriteStateAsync();
        }

        public Task Unsubscribe(IRegistryListener<K, T> listener, String @ref = "")
        {
            var key = Tuple.Create(listener, @ref);
            State.Subscriptions.Remove(key);
            SubscriptionFilterCache.Remove(key);
            return base.WriteStateAsync();
        }
    }

    // HACK: Needed so that Orleans can find the Grain types
    public class AgentRegistry : Registry<Guid, GrainInterfaces.Agent.AgentInfo>
    {
        public AgentRegistry(ILogger<AgentRegistry> logger) : base(logger) { }
    }
    public class ComponentRegistry : Registry<Guid, GrainInterfaces.Component.ComponentInfo>
    {
        public ComponentRegistry(ILogger<ComponentRegistry> logger) : base(logger) { }
    }
    public class FacadeRegistry : Registry<Guid, GrainInterfaces.Facade.FacadeInfo>
    {
        public FacadeRegistry(ILogger<FacadeRegistry> logger) : base(logger) { }
    }
}
