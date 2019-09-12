package ai.scynet.executor

import ai.scynet.protocol.TRAINED
import ai.scynet.protocol.TrainingJob
import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.lang.IgniteRunnable
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.inject
import org.koin.dsl.module
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.*

class IgniteExecuteJob: IgniteRunnable, KoinComponent {

    lateinit var trainingJob: TrainingJob;
    lateinit var cacheName: String;
    protected val ignite: Ignite by inject()

    override fun run() {
        startExecutor()
    }

    constructor(tJob: TrainingJob, cacheName: String){
        this.trainingJob = tJob
        this.cacheName = cacheName
    }

    private fun startExecutor(){

        println("WARNING: Initializing Executor...")
//        val dataXPath = "./trainer/src/main/kotlin/mock/temp/data/xbnc_n_TEMP${trainingJob.UUID}.npy"
//
//        // TODO: We don't even need to use csv when passing stuff to the trainer, that's cool, but discuss
//        Nd4j.writeAsNumpy(trainingJob.dataset?.get("x"), File(dataXPath))

        // In order to use caches carefree as intermediary (shared with the thin client) inputs you need to use only primary types
        ignite.getOrCreateCache<String, TrainingJob>("executorJobs").put(trainingJob.UUID.toString(), trainingJob)

        // The arguments are as follows --output_cache_name --cache_name --UUID
        val pb = ProcessBuilder("bash", "./executor/src/main/python/startExecutor.sh", cacheName, "executorJobs", trainingJob.UUID.toString())

        pb.redirectErrorStream(true)
        val p = pb.start()
        val output  = BufferedReader(InputStreamReader(p.inputStream))

        // ignite.getOrCreateCache<Long, Any>(cacheName).put(timestamp, trainingJob)
        // TODO: So the thin client stops when a new Complex Object using compositions is in the cache

        for(out in output.lines()) {

            println("Python[OUT]: ${out}")

            if (out.split("=")[0] == "DONE") {
                println("DONE")
            }
        }
    }
}