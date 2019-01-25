using System;
using System.Threading.Tasks;
using BlockchainHarvester.GrainInterfaces;
using Orleans;
using Orleans.Streams;

namespace BlockchainHarvester.Grains
{
    public class BlockChainExtractorState
    {
        public int LastBlock { get; set; }
    }

    public class BlockChainExtractor: Grain<BlockChainExtractorState>, IBlockChainExtractor
    {
        private IAsyncStream<byte[]> _stream;
        public BlockChainExtractor()
        {
            
        }

        public override Task OnActivateAsync()
        {
            var streamProvider = this.GetStreamProvider("SMSProvider");
            _stream = streamProvider.GetStream<byte[]>(Guid.Parse("db260371-c425-41df-b729-f530c2367bb5"), "hello");

            return _stream.OnNextAsync(new byte[] {0xFF, 0xDE, 0xAD, 0xBE, 0xEF});
        }

        public async Task DoWork()
        {
            await _stream.OnNextAsync(new byte[] { 0xFF, 0xDE, 0xAD, 0xBE, 0xEF });
        }
    }
}
