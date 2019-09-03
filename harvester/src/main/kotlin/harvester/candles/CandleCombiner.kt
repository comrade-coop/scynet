package harvester.candles

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.ignite.services.ServiceContext
import processors.LazyStreamService

class CandleCombiner: LazyStreamService<Pair<CandleDTO, CandleDTO>>() {
    private val firstMap: HashMap<Long, CandleDTO> = HashMap()
    private val secondMap: HashMap<Long, CandleDTO> = HashMap()
    override fun execute(ctx: ServiceContext?) {
        super.execute(ctx)
        runBlocking {
            launch {
                inputStreams[0].listen{timestamp: Long, candle: CandleDTO, _ ->
                    println("\nCandleOne -> $candle\n")
                    firstMap.put(timestamp, candle)
                    if(secondMap.containsKey(timestamp))
                        streamCombined(timestamp)
                }
            }
            launch {
                inputStreams[1].listen{timestamp: Long, candle: CandleDTO, _ ->
                    print("\n CandleTwo -> $candle\n")
                    secondMap.put(timestamp, candle)
                    if(firstMap.containsKey(timestamp))
                        streamCombined(timestamp)
                }
            }
        }
    }

    private fun streamCombined(timestamp: Long){
        cache.put(timestamp, Pair(firstMap.get(timestamp)!!, secondMap.get(timestamp)!!))
    }

    override fun cancel(ctx: ServiceContext?) {
        inputStreams[0].dispose()
        inputStreams[1].dispose()
        super.cancel(ctx)
    }
}