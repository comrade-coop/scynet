package ai.scynet.trainer

import ai.scynet.protocol.TrainingJob
import org.apache.ignite.services.ServiceContext
import processors.LazyStreamService

class TrainerService: LazyStreamService<Long, TrainingJob>() {
    override fun execute(ctx: ServiceContext?) {
        super.execute(ctx)
        val compute = ignite.compute().withAsync()

        if (inputStreams.size != 0) {
            // listen to the first input stream TODO: Discuss?
            inputStreams[0].listen { timestamp: Long, trainingJob: TrainingJob, _ ->
                logger.info("TrainerSerivce ($timestamp): $trainingJob")
                logger.info("Training Job $timestamp")

                // IgniteTrainingJob should use trainingJob to get data, model and weights
                compute.run(IgniteTrainingJob(trainingJob, ::addToStreamFunc))
            }
        }
    }

    fun addToStreamFunc(t: Long, tJob: TrainingJob) {
        cache.put(t, tJob)
    }
}


