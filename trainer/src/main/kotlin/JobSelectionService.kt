package ai.scynet.trainer

import ai.scynet.protocol.TrainingJob
import org.apache.ignite.services.ServiceContext
import processors.LazyStreamService

class JobSelectionService: LazyStreamService<TrainingJob>() {
    override fun execute(ctx: ServiceContext?) {
        super.execute(ctx)
        inputStreams[0].listen{timestamp: Long, trainingJob: TrainingJob, _ ->
            if(selectJob(trainingJob))
                cache.put(timestamp, trainingJob)
        }
    }

    override fun cancel(ctx: ServiceContext?) {
        inputStreams[0].dispose()
        super.cancel(ctx)
    }

    private fun selectJob(trainingJob: TrainingJob): Boolean{
        return true
    }
}