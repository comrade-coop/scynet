package ai.scynet.trainer

import ai.scynet.protocol.TrainingJob
import org.apache.ignite.services.ServiceContext
import processors.LazyStreamService


class TrainerService: LazyStreamService<Long, TrainingJob>() {
    override fun execute(ctx: ServiceContext?) {
        super.execute(ctx)
        val compute = ignite.compute().withAsync()
        inputStreams[0].listen { timestamp: Long, trainingJob: TrainingJob, _ ->
            //IgniteTrainingJob should use trainingJob to get data, model and weights
            compute.run(IgniteTraingJob())
        }
    }

    override fun cancel(ctx: ServiceContext?) {
        inputStreams[0].dispose()
        super.cancel(ctx)
    }
}


