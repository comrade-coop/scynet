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

        // GET api/values/5
        [HttpGet("{id}")]
        public ActionResult<string> Get(int id)
        {
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
