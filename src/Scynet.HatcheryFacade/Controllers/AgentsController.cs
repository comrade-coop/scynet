using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using Orleans;
using Scynet.GrainInterfaces.Agent;
using Scynet.GrainInterfaces.Component;
using Scynet.GrainInterfaces.Registry;
using ILogger = Grpc.Core.Logging.ILogger;

namespace Scynet.HatcheryFacade.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class AgentsController : ControllerBase
    {
        private readonly IClusterClient ClusterClient;
        private readonly ILogger<AgentsController> _logger;

        public AgentsController(IClusterClient clusterClient, ILogger<AgentsController> logger)
        {
            ClusterClient = clusterClient;
            _logger = logger;
        }

        // GET api/agents
        [HttpGet]
        public async Task<ActionResult<IDictionary<string, List<KeyValuePair<Guid, AgentInfo>>>>> Get()
        {
            // HACK: testing code below
            var registry = ClusterClient.GetGrain<IRegistry<Guid, AgentInfo>>(0);
            //await registry.Register(Guid.Empty, new AgentInfo { });
            //await registry.Register(Guid.NewGuid(), new AgentInfo { });
            //await registry.Register(Guid.NewGuid(), new AgentInfo { });

            var agentGroup = await registry.Query(agents =>
                from agent in agents
                group agent by agent.Value.RunnerType into category
                select Tuple.Create(category.Key, category.ToList()));


            return agentGroup.ToDictionary(category => category.Item1, category => category.Item2);
        }

        // HACK: Needed for testing
        private class TestListener : IRegistryListener<Guid, AgentInfo>
        {
            public void NewItem(string @ref, Guid key, AgentInfo thing)
            {
                Console.WriteLine("Received test notification for {0}: {1}", @ref, key, thing);
            }
        };

        // HACK: Needed for testing
        private class TestEngager : IEngager
        {
            public void Released(IAgent agent)
            {
                Console.WriteLine("Released agent");
            }
        };

        // GET api/agents/5
        [HttpGet("{id}")]
        public async Task<string> Get(int id)
        {
            // HACK: testing code below
            var test = new TestListener();
            var testWrap = await ClusterClient.CreateObjectReference<IRegistryListener<Guid, AgentInfo>>(test);
            var x = ClusterClient.GetGrain<IRegistry<Guid, AgentInfo>>(0);
            await x.Subscribe((k, v) => k == Guid.Empty, testWrap, "test");

            return "value";
        }

        // GET api/agents/engage/{uuid}
        [Route("engage/{uuid}")]
        public async Task<ActionResult<string>> Engage(string uuid)
        {
            // HACK: testing code below
            var test = new TestEngager();
            var testWrap = await ClusterClient.CreateObjectReference<IEngager>(test);
            var id = Guid.Parse(uuid);
            //var x = ClusterClient.GetGrain<IAgent>(id, "Scynet.Grains.ComponentAgent");
            var registry = ClusterClient.GetGrain<IRegistry<Guid, AgentInfo>>(0);
            var agentInfo = await registry.Get(id);
            var engagements2 = await agentInfo.Agent.GetActiveEngagements();
            await agentInfo.Agent.Engage(testWrap);
            var engagements = await agentInfo.Agent.GetActiveEngagements();
            return $"Engagement completed";
        }

        // POST api/values
        [HttpPost]
        public void Post([FromBody] string value)
        {
        }

        // PUT api/values/5
        [HttpPut("{id}")]
        public void Put(int id, [FromBody] string value)
        {
        }

        // DELETE api/values/5
        [HttpDelete("{id}")]
        public void Delete(int id)
        {
        }
    }
}
