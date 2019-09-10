package ai.scynet.executor

import ai.scynet.protocol.*
import org.apache.ignite.services.ServiceContext
import processors.LazyStreamService

class ExecutorService: LazyStreamService<Any>() {

    protected lateinit var currentTrainingJob: TrainingJob
    protected var running: Boolean = false
    protected var initializing: Boolean = false

    override fun init(ctx: ServiceContext?) {
        super.init(ctx)
    }

    override fun execute(ctx: ServiceContext?) {
        super.execute(ctx)

        inputStreams[0].listen { timestamp: Long, trainingJob: TrainingJob, _ ->
            if (isBetter(trainingJob)) {
                println("Initializing executor for $trainingJob")
                initExecutor(trainingJob)
            }
        }

        //inputStreams[1] this should be dataX
        // TODO: Get results from the executor and cache.put them, or just send the executor the cache name or a callback to a function here
    }

    private fun changeOfStatus(running: Boolean, initializing: Boolean) {
        // Callback for the listener of change of status
        this.running = running
        this.initializing = initializing
    }

    private fun initExecutor(tJob: TrainingJob) {
        if (running) {
            // TODO: init, store stuff via cacheID from cache.name or something, this.listen {} process.D
        } else if (initializing) {
            // TODO: Stop the process and init from the begining, or just wait to initialize
        } else {
            // TODO: init via magic process communication
        }
    }

    private fun isBetter(trainingJob: TrainingJob) : Boolean {

        var lossCurrent: Float = currentTrainingJob.status.results["performance"] as Float
        var lossCandidate: Float = trainingJob.status.results["performance"] as Float

        if (lossCandidate < lossCurrent) {
            return true
        }

        return false
    }

    override fun cancel(ctx: ServiceContext?) {
        super.cancel(ctx)
    }

}
