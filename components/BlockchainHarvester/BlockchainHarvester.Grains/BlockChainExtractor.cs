using BlockchainHarvester.GrainInterfaces;
using Orleans;

namespace BlockchainHarvester.Grains
{
    public class BlockChainExtractorState
    {
        public int LastBlock { get; set; }
    }

    public class BlockChainExtractor: Grain<BlockChainExtractorState>, IBlockChainExtractor
    {
        
    }
}
