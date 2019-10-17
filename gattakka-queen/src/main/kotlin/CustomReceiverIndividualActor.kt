package ai.scynet.queen

import IgniteEvaluatingJob
import com.obecto.gattakka.genetics.Genome
import org.apache.ignite.Ignite
import org.apache.ignite.lang.IgniteFuture
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.nd4j.linalg.api.ndarray.INDArray
import java.lang.Exception
import java.util.*
import java.util.concurrent.CompletableFuture
import ai.scynet.protocol.*
import org.apache.ignite.cache.query.ContinuousQuery
import org.apache.ignite.cache.query.ScanQuery
import org.apache.logging.log4j.LogManager

fun Any.f(): Unit {

}

fun <V> IgniteFuture<V>.toCompletableFuture(): CompletableFuture<V> {
    val future = CompletableFuture<V>()
    this.listen { fut ->
        try {
            val res = fut.get();
            future.complete(res);
        }catch(e: Exception){
            future.completeExceptionally(e);
        }
    };
    return future;
}


class CustomReceiverIndividualActor(genome: Genome?) : CustomIndividualActor(genome), KoinComponent {
    val ignite: Ignite by inject()
    private val logger = LogManager.getLogger(this::class.qualifiedName)
    private val datasetCache = ignite.getOrCreateCache<String, Pair<INDArray, INDArray>>("dataset")
    val tempJobCache = ignite.getOrCreateCache<Long, TrainingJob>("tmp_jobs")
    val tempJobCacheIn = ignite.getOrCreateCache<String, Double>("tmp_perf")
    private var agentIsTrained = false
    override fun startProcess() {

        var genome = strategy()

//        var dataX = Nd4j.readNumpy("gattakka-queen/src/main/kotlin/xbnc_n.csv", ",")
//        var dataY = Nd4j.readNumpy("gattakka-queen/src/main/kotlin/ybnc_n.csv", ",")

//        var dataDictionary: HashMap<String, INDArray> = hashMapOf("x" to dataX, "y" to dataY)

        val jobId = UUID.randomUUID()

        var date = Date().time
        var job = TrainingJob(
                jobId,
                "jJASDJnKLkmkLMkMLKML",
                "trainerCluster",
                "basic",
                genome,
                hashMapOf(),
                UNTRAINED() // scynet protocol
        )
        tempJobCache.put(date, job)

        continuouslyDispatchFitness(jobId)
        //Throws null pointer exception. Dig in if you have time
        //evaluateOnNewDataset(jobId)


        //this.dispatchFitness(.5, .5, 100)
    }

    private fun evaluateOnNewDataset(jobId: UUID){
        val query = ContinuousQuery<String, Pair<INDArray, INDArray>>()

        query.setLocalListener{
            it.forEach {
                logger.debug("Dataseet Updated")
                if(agentIsTrained){
                    logger.debug("Agent trained. Starting evaluation on latest dataset.")
                    ignite.compute().run(IgniteEvaluatingJob(jobId.toString()))
                }
            }
        }
        datasetCache!!.query(query)
    }
    private fun continuouslyDispatchFitness(jobId: UUID){
        val query = ContinuousQuery<String, Double>()

        query.setLocalListener { evts ->
            run {
                evts.forEach { e ->
                    run {
                        logger.debug("e.key -> ${e.key} e.value -> ${e.value}")
                        if (e.key == jobId.toString()){
                            if(!agentIsTrained){
                                agentIsTrained = true
                            }
                            if(e.value == null){
                                logger.error("value of performance is null!")
                            }
                            this@CustomReceiverIndividualActor.dispatchFitness(e.value, .5, 100)
                        }
                    }
                }
            }
        }

        query.initialQuery = ScanQuery<String,Double>()

        tempJobCacheIn!!.query(query)
    }


}
