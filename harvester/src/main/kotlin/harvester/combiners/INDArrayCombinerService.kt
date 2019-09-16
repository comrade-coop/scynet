package harvester.combiners

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.ignite.services.ServiceContext
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import processors.LazyStreamService

class INDArrayCombinerService: LazyStreamService<Long, INDArray>() {
    private val firstMap: HashMap<Long, INDArray> = HashMap()
    private val secondMap: HashMap<Long, INDArray> = HashMap()
    override fun execute(ctx: ServiceContext?) {
        super.execute(ctx)
        runBlocking {
            launch {
                inputStreams[0].listen{ timestamp: Long, array: INDArray, _ ->
                    firstMap.put(timestamp, array)
                    if(secondMap.containsKey(timestamp))
                        streamCombined(timestamp)
                }
            }
            launch {
                inputStreams[1].listen{ timestamp: Long, array: INDArray, _ ->
                    secondMap.put(timestamp, array)
                    if(firstMap.containsKey(timestamp))
                        streamCombined(timestamp)
                }
            }
        }
    }

    private fun streamCombined(timestamp: Long){
        val combined = Nd4j.hstack(firstMap.get(timestamp), secondMap.get(timestamp))
        cache.put(timestamp, combined)
        firstMap.remove(timestamp)
        secondMap.remove(timestamp)
    }
}