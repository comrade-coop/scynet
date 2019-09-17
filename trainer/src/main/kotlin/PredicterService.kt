package ai.scynet.trainer

import ai.scynet.protocol.TrainingJob
import org.apache.ignite.services.ServiceContext
import org.nd4j.linalg.api.ndarray.INDArray
import processors.LazyStreamService


class PredicterService: LazyStreamService<Long, String>() {

    override fun init(ctx: ServiceContext?) {
        super.init(ctx)
    }

    override fun execute(ctx: ServiceContext?) {
        super.execute(ctx)
        val compute = ignite.compute().withAsync()

        if (inputStreams.size != 0) {
            // listen to the first input stream TODO: Discuss?
            inputStreams[0].listen { _: Long, x: INDArray, _ ->
                compute.run(IgnitePredictingJob(
                        descriptor!!.properties?.get("id") as String,
                        descriptor!!.properties?.get("egg") as String,
                        x
                ) { p: Double -> cache.put(System.currentTimeMillis(), p.toString()) })
            }
        }
    }

    override fun cancel(ctx: ServiceContext?) {
        inputStreams[0].dispose()
        super.cancel(ctx)
    }
}


