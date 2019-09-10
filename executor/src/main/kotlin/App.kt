package ai.scynet.executor

import ai.scynet.executor.OutputStream
import ai.scynet.executor.QueryStream
import ai.scynet.trainer.mock.MockJobsStream
import descriptors.Properties
import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.apache.ignite.configuration.IgniteConfiguration
import org.koin.core.context.startKoin
import org.koin.dsl.module
import processors.ILazyStreamFactory
import processors.LazyStreamFactory
import java.util.*

/**

 * The first function to be executed
 * @param args The arguments we are receiving
 */
fun main(args: Array<String>) {

    println("Starting executor...")
    val cfg = IgniteConfiguration()
    cfg.igniteInstanceName = "Executor Integration Test"
    val ignite = Ignition.start(cfg)

    startKoin {
        printLogger()
        modules(module {
            single<Ignite> { ignite }
        })
    }

    // Global identifiers
    val LAZY_STREAM_FACTORY = "lazyStreamFactory"

    // Deploying services
    ignite.services().deployClusterSingleton(LAZY_STREAM_FACTORY, LazyStreamFactory())
    val streamManager = ignite.services().serviceProxy(LAZY_STREAM_FACTORY, ILazyStreamFactory::class.java, false)

    val mockJobsStreamID = UUID.randomUUID()

    val mockJobsStream = MockJobsStream(mockJobsStreamID, null, Properties())

    val queryStreamID = UUID.randomUUID()
    // Implementing the QueryService as the interface between the web facade and the actual stream
    val queryStream = QueryStream(queryStreamID, ArrayList<UUID>().apply { add(mockJobsStreamID) }, Properties())

    val dataXStreamID = UUID.randomUUID()
    //  val dataXStream = DataStream() // Десо data stream here?

    val outputStreamID = UUID.randomUUID()
    val outputResultStream = OutputStream(outputStreamID, ArrayList<UUID>().apply { add(queryStreamID); add(dataXStreamID) }, Properties())

    streamManager.registerStream(mockJobsStream)
    streamManager.registerStream(queryStream)
    streamManager.registerStream(outputResultStream)

    var outputResultsStreamProxy = streamManager.getInstance(outputStreamID)

    println("WAITING FOR DATA FINALLY")

    var results = outputResultsStreamProxy.listen { t: Long, j: Any, _ ->
        println("INFO: Result #$t: $j")
    }
}

