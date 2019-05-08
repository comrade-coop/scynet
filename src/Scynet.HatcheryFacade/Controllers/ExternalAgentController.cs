using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.Web;
using Microsoft.AspNetCore.Mvc;
using Orleans;
using Scynet.GrainInterfaces.Agent;
using Scynet.GrainInterfaces.Component;
using Scynet.GrainInterfaces.Registry;

namespace Scynet.HatcheryFacade.Controllers
{
    // [Route("api/[controller]")]
    // [ApiController]
    public class ExternalAgentController : ControllerBase
    {
        private IClusterClient ClusterClient;

        public ExternalAgentController(IClusterClient clusterClient)
        {
            ClusterClient = clusterClient;
        }

        [HttpGet]
        public async Task<IEnumerable<string>> Get()
        {
            var registry = ClusterClient.GetGrain<IRegistry<Guid, AgentInfo>>(0);
            var res = await registry.Query(x => from p in x where p.Value.IsExternal() select p.Key);
            return res.Select(r => r.ToString());
        }

        [HttpGet("{id}")]
        public async Task<ActionResult<string>> Get(Guid id)
        {
            var registry = ClusterClient.GetGrain<IRegistry<Guid, AgentInfo>>(0);
            var agent = await registry.Get(id);
            if (agent != null && agent.IsExternal())
            {
                return await (agent.Agent as IExternalAgent).GetAddress();
            }
            else
            {
                return new StatusCodeResult(404);
            }
        }

        [HttpPut("{id}")]
        public async void Put(Guid id, [FromBody] string newAddress)
        {
            var agent = ClusterClient.GetGrain<IExternalAgent>(id);

            await agent.Initialize(new AgentInfo()
            {
                ComponentId = Guid.Empty,
                RunnerType = "",
                OutputShapes = new List<List<uint>>() { new List<uint>() { 1 } }, // TODO: Get from remote hatchery
                Frequency = 0,
                Agent = agent,
            }, newAddress);
        }

        [HttpDelete("{id}")]
        public async void Delete(Guid id)
        {
            var agent = ClusterClient.GetGrain<IExternalAgent>(id);

            await agent.ReleaseAll();
            await agent.Initialize(null, "");

            var registry = ClusterClient.GetGrain<IRegistry<Guid, AgentInfo>>(0);
        }
    }
}
