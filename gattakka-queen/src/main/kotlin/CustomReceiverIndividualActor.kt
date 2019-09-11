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

    override fun startProcess() {
//        println(ignite)
////        val gene = genome!!.chromosomes().head().toGene()
////
////        tempJobCache.putAsync("name", gene).toCompletableFuture().get()
////
////        println(gene)
////        return Option.empty()

//        var mockModel = File("src/main/kotlin/mockModel.json").inputStream().readBytes().toString(Charsets.UTF_8)
        var genome = strategy()

        var dataX = Nd4j.readNumpy("gattakka-queen/src/main/kotlin/xbnc_n.csv", ",")
        var dataY = Nd4j.readNumpy("gattakka-queen/src/main/kotlin/ybnc_n.csv", ",")

        var dataDictionary: HashMap<String, INDArray> = hashMapOf("x" to dataX, "y" to dataY)


        var date = Date().time
        var job = TrainingJob(
                UUID.randomUUID(),
                "jJASDJnKLkmkLMkMLKML",
                "trainerCluster",
                "basic",
                genome,
                dataDictionary,
                UNTRAINED() // scynet protocol
        )
        tempJobCache.put(date, job)

        //this.dispatchFitness(.5, .5, 100)
    }


}
