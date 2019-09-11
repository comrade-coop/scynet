package harvester.windows

import org.apache.ignite.services.ServiceContext
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import processors.LazyStreamService
import java.util.*

class WindowingService: LazyStreamService<Long, INDArray>() {
    private val window: LinkedList<Pair<Long, INDArray>> = LinkedList()
    private var windowSize: Int? = null

    override fun init(ctx: ServiceContext?) {
        super.init(ctx)
        windowSize = descriptor!!.properties!!.get("windowSize") as Int
    }
    override fun execute(ctx: ServiceContext?) {
        super.execute(ctx)
        inputStreams[0].listen{ timestamp: Long, combinedCandle: INDArray, _ ->
            window.addLast(Pair(timestamp,combinedCandle))
            if(window.size == windowSize!!){
                val windowed = getWindowed()
                cache.put(window.last.first, windowed)
                window.removeFirst()
            }
        }
    }
    private fun getWindowed(): INDArray{
        var vstack: INDArray? = null
        for(timestampCandle in window){
            if(vstack == null){
                vstack = timestampCandle.second
            }else{
                vstack = Nd4j.vstack(vstack, timestampCandle.second)
            }
        }
        return vstack!!
    }
}