package ai.scynet.queen

import com.obecto.gattakka.genetics.Genome
import com.obecto.gattakka.genetics.descriptors.Gene
import org.apache.ignite.Ignite
import org.apache.ignite.lang.IgniteFuture
import org.koin.core.KoinComponent
import org.koin.core.inject
import scala.Option
import java.lang.Exception
import java.util.concurrent.CompletableFuture

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

    val tempJobCache = ignite.getOrCreateCache<String, Gene>("Jobs")

//    override fun receiveGenome(genome: Genome?, data: Any?): Option<Any> {
//        println(ignite)
//        val gene = genome!!.chromosomes().head().toGene()
//
//        tempJobCache.putAsync("name", gene).toCompletableFuture().get()
//
//        println(gene)
//        return Option.empty()
//        this.dispatchFitness(0.5)
//    }


}
