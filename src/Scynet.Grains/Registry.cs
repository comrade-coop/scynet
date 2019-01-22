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

        public Task<List<T>> QueryAgents(ExpressionNode expression)
        {
            var compiledExpression = expression.ToExpression<Func<List<T>, List<T>>>().Compile();
            List<T> items = compiledExpression(State.Items);
            return Task.FromResult(items);
        }

        public Task<K> Sheny<K>(ExpressionNode expression)
        {
            var compiledExpression = expression.ToExpression<Func<List<T>, K>>().Compile();
            K items = compiledExpression(State.Items);
            return Task.FromResult(items);
        }

       public void QueryAgents()
        {
            List<AgentInfo> data = new List<AgentInfo>();
            var query = from c in data
                        select c.Id;
            var z = query.AsQueryable();

            Expression<Func<int>> sum = () => 1 + 2;

            //parameter expression where AgentInfo is the type of the parameter and 's' is the name of the parameter
            ParameterExpression pe = Expression.Parameter(typeof(AgentInfo), "s");
            //use Expression.Property() to create s.Id expression where s is the parameter and Id is the property name of AgentInfo
            MemberExpression me = Expression.Property(pe, "Id");
            //create a constant expression for 18
            ConstantExpression constant = Expression.Constant("0", typeof(string));
            // check whether a member expression is greater than a constant expression or not.
            BinaryExpression body = Expression.Equal(me, constant);

            Console.WriteLine(body);
            Expression<Func<AgentInfo, bool>> e = Expression.Lambda<Func<AgentInfo, bool>>(body, pe);
            var res = data.Where(e.Compile());
        }
    }
}
