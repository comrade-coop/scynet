package harvester.candles

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.ignite.services.ServiceContext
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import processors.LazyStreamService
import java.time.Instant
import java.util.*
import kotlin.collections.HashMap

class CandleCombiner: LazyStreamService<Long, INDArray>() {
    private val firstMap: HashMap<Long, CandleDTO> = HashMap()
    private val secondMap: HashMap<Long, CandleDTO> = HashMap()
    override fun execute(ctx: ServiceContext?) {
        super.execute(ctx)
        runBlocking {
            launch {
                inputStreams[0].listen { timestamp: Long, candle: CandleDTO, _ ->
                    firstMap.put(timestamp, candle)
                    if (secondMap.containsKey(timestamp))
                        streamCombined(timestamp)
                }
            }
            launch {
                inputStreams[1].listen { timestamp: Long, candle: CandleDTO, _ ->
                    secondMap.put(timestamp, candle)
                    if (firstMap.containsKey(timestamp))
                        streamCombined(timestamp)
                }
            }
        }

    }

    private fun streamCombined(timestamp: Long){
        cache.put(timestamp,combine(timestamp))
        firstMap.remove(timestamp)
        secondMap.remove(timestamp)
    }

    private fun combine(timestamp: Long): INDArray{
        //open close high low  2 rows 4 columns
        val r1 = firstMap.get(timestamp)!!.getData()
        val r2 = secondMap.get(timestamp)!!.getData()
        val flat = r1 + r2
        val columns = r1.size
        val shape = intArrayOf(1, columns * 2)
        return Nd4j.create(flat, shape, 'c')
    }
}