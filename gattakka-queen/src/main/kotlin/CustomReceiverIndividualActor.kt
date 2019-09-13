package ai.scynet.queen

import com.obecto.gattakka.genetics.Genome
import com.obecto.gattakka.genetics.descriptors.Gene
import org.apache.ignite.Ignite
import org.apache.ignite.lang.IgniteFuture
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import scala.Option
import java.lang.Exception
import java.util.*
import java.util.concurrent.CompletableFuture
import ai.scynet.protocol.*
import ai.scynet.trainer.TrainingJobsStream
import descriptors.Properties
import org.apache.ignite.cache.query.ContinuousQuery
import org.apache.ignite.cache.query.ScanQuery
import processors.ILazyStreamFactory
import processors.LazyStreamFactory
import java.io.File

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

    val tempJobCache = ignite.getOrCreateCache<Long, TrainingJob>("tmp_jobs")
    val tempJobCacheIn = ignite.getOrCreateCache<String, Double>("tmp_perf")

    override fun startProcess() {

        var genome = strategy()

        var dataX = Nd4j.readNumpy("gattakka-queen/src/main/kotlin/xbnc_n.csv", ",")
        var dataY = Nd4j.readNumpy("gattakka-queen/src/main/kotlin/ybnc_n.csv", ",")

        var dataDictionary: HashMap<String, INDArray> = hashMapOf("x" to dataX, "y" to dataY)

        val jobId = UUID.randomUUID()

        var date = Date().time
        var job = TrainingJob(
                jobId,
                "jJASDJnKLkmkLMkMLKML",
                "trainerCluster",
                "basic",
                genome,
                dataDictionary,
                UNTRAINED() // scynet protocol
        )
        tempJobCache.put(date, job)

        val query = ContinuousQuery<String, Double>()

        query.setLocalListener { evts ->
            run {
                evts.forEach { e ->
                    run {
                        if (e.key == jobId.toString()){
                            this@CustomReceiverIndividualActor.dispatchFitness(e.value, .5, 100)
                        }
                    }
                }
            }
        }

        query.initialQuery = ScanQuery<String,Double>()

        tempJobCacheIn!!.query(query)


        //this.dispatchFitness(.5, .5, 100)
    }


}
