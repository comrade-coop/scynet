package ai.scynet.trainer

import ai.scynet.protocol.TrainingJob
import org.apache.ignite.services.ServiceContext
import processors.LazyStreamService

class JobSelectionService: LazyStreamService<Long, TrainingJob>() {
    override fun execute(ctx: ServiceContext?) {
        super.execute(ctx)

        inputStreams[0].listen{ timestamp: Long, trainingJob: TrainingJob, _ ->
            if(selectJob(trainingJob)) {
                //can set log level to  INFO when more complex selection is implemented
                logger.trace("Selecting $timestamp")
                cache.put(timestamp, trainingJob)
            }
        }
    }

    private fun selectJob(trainingJob: TrainingJob): Boolean{
        return true
    }
}