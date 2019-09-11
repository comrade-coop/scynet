package harvester.normalization

import org.apache.ignite.services.ServiceContext
import org.nd4j.linalg.api.ndarray.INDArray
import processors.LazyStreamService

class NormalizingService: LazyStreamService<Long, INDArray>() {
    override fun execute(ctx: ServiceContext?) {
        super.execute(ctx)
        inputStreams[0].listen{timestamp: Long, window: INDArray, _ ->
            val lastRow = window.getRow(window.rows() - 1)
            window.diviRowVector(lastRow)
            cache.put(timestamp, window)
        }
    }
}