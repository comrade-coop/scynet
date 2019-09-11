package ai.scynet.trainer

import ai.scynet.protocol.TrainingJob
import ai.scynet.trainer.mock.MockJobsStream
import descriptors.Properties
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CORS.Feature.install
import io.ktor.http.ContentType
import io.ktor.http.cio.websocket.*
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.launch
import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.apache.ignite.configuration.IgniteConfiguration
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.nd4j.shade.jackson.databind.ObjectMapper
import processors.ILazyStreamFactory
import processors.LazyStreamFactory
import java.util.*
import kotlin.collections.ArrayList

/**

 * The first function to be executed
 * @param args The arguments we are receiving
 */
fun main(args: Array<String>) {
    val cfg = IgniteConfiguration()
    cfg.igniteInstanceName = "Trainer Integration Test"
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

    // Creating job streams
    val mockJobsStreamID = UUID.randomUUID()
    val mockJobsStream = MockJobsStream(mockJobsStreamID, null, Properties().apply {
        // Test config
        put("configSelectedJobStream", true)
    })

    println("INFO: mockJobsStreamID -> $mockJobsStreamID")

    val selectedJobsStreamID = UUID.randomUUID()
    val selectedJobsStream = SelectedJobsStream(selectedJobsStreamID, ArrayList<UUID>().apply { add(mockJobsStreamID) }, Properties())

    println("INFO: selectedJobsStreamID -> $selectedJobsStreamID")

    val finishedJobsStreamID = UUID.randomUUID()
    val finishedJobsStream = TrainingJobsStream(finishedJobsStreamID, ArrayList<UUID>().apply { add(selectedJobsStreamID) }, Properties())

    println("INFO: finishedJobsStreamID -> $finishedJobsStreamID")

    // Register job stream

    streamManager.registerStream(mockJobsStream)
    streamManager.registerStream(selectedJobsStream)
    streamManager.registerStream(finishedJobsStream)

    // Access stream through a proxy
    var finishedJobStreamProxy = streamManager.getInstance(finishedJobsStreamID)


    // TODO: Move to an external file.
    val objectMapper = ObjectMapper()
    val jobList = mutableListOf<TrainingJob>()
    embeddedServer(Netty, 8080) {
        install(WebSockets)
		routing {
            val wsConnections = Collections.synchronizedSet(LinkedHashSet<DefaultWebSocketSession>())

            var jobs =  finishedJobStreamProxy.listen { t: Long, j: TrainingJob, _ ->
                println("INFO: Job $t Finished $j")
                val clean = j.copy(dataset = null)
                val json = objectMapper.writeValueAsString(clean)

                jobList.add(clean)

                launch {
                    wsConnections.forEach {
                        it.outgoing.send(Frame.Text(json))
                    }
                }

            }

            get("/") {
				call.respondText(objectMapper.writeValueAsString(jobList), ContentType.Application.Json)
			}

            webSocket("/jobs") { // websocketSession
                wsConnections += this
                try{
                    for (frame in incoming) {
                        when (frame) {
                            is Frame.Text -> {
                                val text = frame.readText()
                                outgoing.send(Frame.Text("YOU SAID: $text"))
                                if (text.equals("bye", ignoreCase = true)) {
                                    close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                                }
                            }
                        }
                    }
                }finally{
                    wsConnections -= this
                }
            }
		}

	}.start(wait = true)
}

