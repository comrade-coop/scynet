using System;
using System.Collections.Generic;
using System.Text;
using System.Threading.Tasks;
using Orleans;

namespace BlockchainHarvester.GrainInterfaces
{
    public interface IBlockChainExtractor: IGrainWithIntegerKey
    {
        Task DoWork();
    }
}
