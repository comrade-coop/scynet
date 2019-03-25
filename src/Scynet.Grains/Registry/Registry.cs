using System;
using System.Collections.Generic;
using System.Linq;
using System.Linq.Expressions;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using Serialize.Linq.Nodes;
using Scynet.GrainInterfaces.Registry;

namespace Scynet.Grains.Registry
{
    public class RegistrySubscription
    {
        public ExpressionNode Filter;
        public SubscriptionOptions Options;
    }

    public class RegistryState<K, T>
    {
        public Dictionary<K, T> Items = new Dictionary<K, T>();
        // TODO: Can the below line be shortened somehow?
        public Dictionary<Tuple<IRegistryListener<K, T>, String>, RegistrySubscription> Subscriptions =
            new Dictionary<Tuple<IRegistryListener<K, T>, String>, RegistrySubscription>();
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
            var isOld = State.Items.ContainsKey(key);
            State.Items[key] = info;
            foreach (var subscription in State.Subscriptions)
            {
                if (isOld && subscription.Value.Options == SubscriptionOptions.OnlyNew)
                {
                    continue;
                }

                if (!SubscriptionFilterCache.ContainsKey(subscription.Key))
                {
                    SubscriptionFilterCache[subscription.Key] =
                        subscription.Value.Filter.ToExpression<Func<K, T, bool>>().Compile();
                }

                var filter = SubscriptionFilterCache[subscription.Key];
                if (filter(key, info))
                {
                    var listener = subscription.Key.Item1;
                    var @ref = subscription.Key.Item2;
                    listener.NewItem(@ref, key, info);
                }
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

        public Task Subscribe(ExpressionNode expression, IRegistryListener<K, T> listener, String @ref = "", SubscriptionOptions options = SubscriptionOptions.Default)
        {
            var key = Tuple.Create(listener, @ref);
            SubscriptionFilterCache.Remove(key);
            State.Subscriptions[key] = new RegistrySubscription
            {
                Filter = expression,
                Options = options,
            };
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
    public class AgentRegistry : Registry<Guid, GrainInterfaces.Component.AgentInfo>
    {
        public AgentRegistry(ILogger<AgentRegistry> logger) : base(logger) { }
    }
    public class ComponentRegistry : Registry<Guid, GrainInterfaces.Component.ComponentInfo>
    {
        public ComponentRegistry(ILogger<ComponentRegistry> logger) : base(logger) { }
    }
}
