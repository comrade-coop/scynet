package ai.scynet.trainer

import ai.scynet.protocol.TrainingJob
import org.apache.ignite.services.ServiceContext
import processors.LazyStreamService


class TrainerService: LazyStreamService<TrainingJob>() {

    override fun execute(ctx: ServiceContext?) {
        super.execute(ctx)
        val compute = ignite.compute().withAsync()

        if (inputStreams.size != 0) {
            // listen to the first input stream TODO: Discuss?
            inputStreams[0].listen { timestamp: Long, trainingJob: TrainingJob, _ ->
                println("INFO: TrainerSerivce ($timestamp): $trainingJob")
                println("INFO: Training Job $timestamp")

                // IgniteTrainingJob should use trainingJob to get data, model and weights
                compute.run(IgniteTrainingJob(trainingJob, ::addToStreamFunc))
            }
        }
    }

    fun addToStreamFunc(t: Long, tJob: TrainingJob) {
        cache.put(t, tJob)
    }

    override fun cancel(ctx: ServiceContext?) {
        inputStreams[0].dispose()
        super.cancel(ctx)
    }
}


