using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Orleans;
using Scynet.GrainInterfaces;

namespace Scynet.HatcheryFacade.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class ValuesController : ControllerBase
    {
        private IClusterClient ClusterClient;

        public ValuesController(IClusterClient clusterClient) {
            ClusterClient = clusterClient;
            Console.WriteLine(clusterClient);
        }

        // GET api/values
        [HttpGet]
        public async Task<ActionResult<IEnumerable<string>>> Get()
        {
            // HACK: testing code below
            var registry = ClusterClient.GetGrain<IRegistry<AgentInfo>>(0);
            await registry.Register(new AgentInfo { Id = Guid.Empty });
            await registry.Register(new AgentInfo { Id = Guid.NewGuid() });
            await registry.Register(new AgentInfo { Id = Guid.NewGuid() });

            var res = await registry.Query(x =>
                from y in x
                    where y.Id == Guid.Empty
                    select y);
            Console.WriteLine("----------");
            Console.WriteLine(String.Join('|', from x in res select x.Id));
            Console.WriteLine(res.GetType());
            return new string[] { "value1", "value2" };
        }

        // HACK: Needed for testing
        private class TestListener : IRegistryListener<AgentInfo> {
            public void NewItem(string @ref, AgentInfo thing) {
                Console.WriteLine("Received test notification for {0}: {1}", @ref, thing.Id);
            }
        };

        // GET api/values/5
        [HttpGet("{id}")]
        public async Task<ActionResult<string>> Get(int id)
        {
            // HACK: testing code below
            var test = new TestListener();
            var testWrap = await ClusterClient.CreateObjectReference<IRegistryListener<AgentInfo>>(test);
            var x = ClusterClient.GetGrain<IRegistry<AgentInfo>>(0);
            await x.Subscribe(y => y.Id == Guid.Empty, testWrap, "test");

            return "value";
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
