using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Orleans;
using Scynet.GrainInterfaces.Agent;
using Scynet.GrainInterfaces.Component;
using Scynet.GrainInterfaces.Registry;

namespace Scynet.HatcheryFacade.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class ValuesController : ControllerBase
    {
        private IClusterClient ClusterClient;

        public ValuesController(IClusterClient clusterClient)
        {
            ClusterClient = clusterClient;
            Console.WriteLine(clusterClient);
        }

        // GET api/values
        [HttpGet]
        public async Task<ActionResult<IEnumerable<string>>> Get()
        {
            // HACK: testing code below
            var registry = ClusterClient.GetGrain<IRegistry<Guid, AgentInfo>>(0);
            //await registry.Register(Guid.Empty, new AgentInfo { });
            //await registry.Register(Guid.NewGuid(), new AgentInfo { });
            //await registry.Register(Guid.NewGuid(), new AgentInfo { });

            var res = await registry.Query(x =>
                from y in x
                select y);


            return res.Select(pair => pair.Key.ToString()).ToArray();
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

        // GET api/values/5
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

        // GET api/values/xxx
        [Route("Engage/{uuid}")]
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
            return "engagement completed";
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
