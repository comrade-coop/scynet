using System;
using System.Collections.Generic;
using System.Linq;
using System.Linq.Expressions;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.HttpsPolicy;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using Orleans;
using Orleans.Configuration;
using Scynet.GrainInterfaces;
using Serialize.Linq.Extensions;

namespace Scynet.HatcheryFacade
{
    public class Startup
    {
        public Startup(IConfiguration configuration)
        {
            Configuration = configuration;
        }

        public IConfiguration Configuration { get; }

        // This method gets called by the runtime. Use this method to add services to the container.
        public void ConfigureServices(IServiceCollection services)
        {
            services.AddMvc().SetCompatibilityVersion(CompatibilityVersion.Version_2_2);
            services.AddSingleton<IClusterClient>(sp => ConnectClient().Result);
        }

        // This method gets called by the runtime. Use this method to configure the HTTP request pipeline.
        public void Configure(IApplicationBuilder app, IHostingEnvironment env)
        {
            if (env.IsDevelopment())
            {
                app.UseDeveloperExceptionPage();
            }
            else
            {
                // The default HSTS value is 30 days. You may want to change this for production scenarios, see https://aka.ms/aspnetcore-hsts.
                // app.UseHsts();
            }

            // app.UseHttpsRedirection();
            app.UseMvc();
        }

        private async Task<IClusterClient> ConnectClient()
        {
            Console.WriteLine("Configuring connection to local silo...");

            var builder = new ClientBuilder()
                .UseLocalhostClustering()
                .Configure<ClusterOptions>(options =>
                {
                    options.ClusterId = "dev";
                    options.ServiceId = "Scynet";
                })
                .ConfigureLogging(logging => logging.AddConsole());

            IClusterClient client = builder.Build();

            Console.WriteLine("Connecting...");

            await client.Connect();

            Console.WriteLine("Client successfully connected to silo host");

            var registry = client.GetGrain<IRegistry<AgentInfo>>(0);
            await registry.Register(new AgentInfo("5"));
            await registry.Register(new AgentInfo("2"));
            await registry.Register(new AgentInfo("4"));

            //var abc = data.Where(s => s.Id.Equals("5")).ToList();
            //Expression<Func<AgentInfo, bool>> expression = x => x.Id.Equals("5");
            //var queryNode = expression.ToExpressionNode();


            //Expression<Func<List<AgentInfo>, List<AgentInfo>>> z = (x => x.Where(s => s.Id.Equals("5")).ToList());
            Expression<Func<List<AgentInfo>, List<AgentInfo>>> zz =
                (x => (from y in x
                       where y.Equals(5)
                       select new AgentInfo(y.Id)
                       ).ToList());
            var queryNode = zz.ToExpressionNode();

            var res = await registry.QueryAgents(queryNode);
            Console.WriteLine(res);

            return client;
        }
    }
}
