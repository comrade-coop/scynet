package harvester.pairs

import harvester.candles.CandleDTO
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.ignite.services.ServiceContext
import org.nd4j.linalg.api.ndarray.INDArray
import processors.LazyStreamService

class PairingService: LazyStreamService<Long, Pair<Boolean, INDArray>>(){

    private val firstMap: HashMap<Long, Boolean> = HashMap()
    private val secondMap: HashMap<Long, INDArray> = HashMap()
    override fun execute(ctx: ServiceContext?) {
        runBlocking {
                launch {
                    inputStreams[0].listen { timestamp: Long, label: Boolean, _ ->
                        firstMap.put(timestamp, label)
                        if (secondMap.containsKey(timestamp))
                            streamPair(timestamp)
                    }
                }
                launch {
                    inputStreams[1].listen { timestamp: Long, normalizedWindow: INDArray, _ ->
                        secondMap.put(timestamp, normalizedWindow)
                        if(firstMap.containsKey(timestamp))
                            streamPair(timestamp)
                    }
                }
        }
    }

    private fun streamPair(timestamp: Long){
        val pair = Pair(firstMap.get(timestamp)!!, secondMap.get(timestamp)!!)
        cache.put(timestamp, pair)
        firstMap.remove(timestamp)
        secondMap.remove(timestamp)
    }
}
