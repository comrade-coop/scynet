package ai.scynet.trainer

import ai.scynet.protocol.Dataset
import ai.scynet.protocol.Model
import ai.scynet.protocol.TrainingJob
import org.apache.ignite.services.ServiceContext
import processors.LazyStreamService
import java.lang.IllegalStateException
import java.security.Timestamp
import java.time.Instant
import java.util.*

class MockService: LazyStreamService<TrainingJob>() {

    override fun init(ctx: ServiceContext?) {
        super.init(ctx)
    }

    override fun execute(ctx: ServiceContext?) {
        super.execute(ctx)

        println("MockJob stream connected")

        for (i in 0..100) {
            var date = Date(2019 + i, 10, 2, 10, 2, 100).time
            var job = TrainingJob(
                    "this one",
                    "trainerCluster",
                    "this one",
                    "nice",
                    null,
                    "Available"
            )
            cache.put(date, job)
        }
    }

    override fun cancel(ctx: ServiceContext?) {
        super.cancel(ctx)
    }

}
