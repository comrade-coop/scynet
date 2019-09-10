package ai.scynet.executor

import ai.scynet.protocol.*
import org.apache.ignite.services.ServiceContext
import org.deeplearning4j.datasets.iterator.FloatsDataSetIterator
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import processors.LazyStreamService
import java.io.File
import java.lang.IllegalStateException
import java.security.Timestamp
import java.sql.Time
import java.time.Instant
import java.util.*

class QueryService: LazyStreamService<TrainingJob>() {

    override fun init(ctx: ServiceContext?) {
        super.init(ctx)
    }

    override fun execute(ctx: ServiceContext?) {
        super.execute(ctx)

        // Use a Web facade to put stuff into the query stream
        inputStreams[0].listen { timestamp: Long, trainingJob: TrainingJob, _ ->
            println("INFO: Found $timestamp: $trainingJob")
            cache.put(timestamp, trainingJob)
        }
    }

    override fun cancel(ctx: ServiceContext?) {
        super.cancel(ctx)
    }

}
