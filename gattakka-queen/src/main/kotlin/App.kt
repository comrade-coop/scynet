package ai.scynet.queen

import ai.scynet.protocol.TrainingJob
import descriptors.Properties
import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.apache.ignite.configuration.IgniteConfiguration
import org.koin.core.context.startKoin
import org.koin.dsl.module
import processors.ILazyStreamFactory
import processors.LazyStreamFactory
import java.util.*
import ai.scynet.trainer.*
import ai.scynet.protocol.StatusID
import ai.scynet.protocol.TRAINED
import io.ktor.application.install
import io.ktor.http.cio.websocket.Frame
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import org.apache.ignite.cache.query.ContinuousQuery
import org.apache.ignite.cache.query.ScanQuery
import org.nd4j.shade.jackson.databind.ObjectMapper

fun main(args: Array<String>) {
	val cfg = IgniteConfiguration()
	cfg.igniteInstanceName = "HarvesterTest"
	//cfg.setPeerClassLoadingEnabled(true)
	val ignite = Ignition.start(cfg)

	startKoin {
		printLogger()
		modules(module {
			single<Ignite> { ignite }
		})
	}
	val LAZY_STREAM_FACTORY = "lazyStreamFactory"
	ignite.services().deployClusterSingleton(LAZY_STREAM_FACTORY, LazyStreamFactory())
	val streamManager = ignite.services().serviceProxy(LAZY_STREAM_FACTORY, ILazyStreamFactory::class.java, false)



	val GattakkaStreamID = UUID.randomUUID()
	println("\nGattakkaStreamID -> $GattakkaStreamID\n")
	val GattakkaStream = GattakkaLazyStream(GattakkaStreamID, null, Properties())


	val selectedJobsStreamID = UUID.randomUUID()
	val selectedJobsStream = SelectedJobsStream(selectedJobsStreamID, ArrayList<UUID>().apply { add(GattakkaStreamID) }, Properties())

	val finishedJobsStreamID = UUID.randomUUID()
	val finishedJobsStream = TrainingJobsStream(finishedJobsStreamID, ArrayList<UUID>().apply { add(selectedJobsStreamID) }, Properties())

	val predictionJobStreamID = UUID.randomUUID()
	println("\npredictionJobStreamID -> $predictionJobStreamID\n")
	val predictionJobStream = PredictingJobsStream(predictionJobStreamID, ArrayList<UUID>().apply { add(finishedJobsStreamID) }, Properties())


	streamManager.registerStream(GattakkaStream)
	streamManager.registerStream(selectedJobsStream)
	streamManager.registerStream(finishedJobsStream)
	streamManager.registerStream(predictionJobStream)




	val tempJobCache_in = ignite.getOrCreateCache<String, Double>("tmp_perf")


	var reportedPerformance = emptyList<String>()

	var streamProxy = streamManager.getInstance(finishedJobsStreamID)
	var cursor =  streamProxy.listen { t:Long, c: TrainingJob, _ ->
		println("\nStream Output for **************************************************************** $t -> $c\n")

		if(c.status.statusID == StatusID.TRAINED) {
			var perf = (c.status as TRAINED).results.getValue("performance")
			var genome = c.egg
			tempJobCache_in.put(c.UUID.toString(), if (perf.toDoubleOrNull() != null) perf.toDouble() else 100.0)
			reportedPerformance = reportedPerformance + listOf(perf) //TODO: Ideally, this line should be one "atomic" operation. Consider thread-safe mutable list.

	  		// Find the genome and then the trained model of the genome

		   	// Pass the trainied model to the newly created IgnitePredictionJob (copy/pase/modiyfy from IgniteTrainingJob
			// Invoke the IgnitePredictionJob
		}
	}

	var streamProxyTemp = streamManager.getInstance(predictionJobStreamID)
	var cursorTemp =  streamProxyTemp.listen { t:Long, c: TrainingJob, _ ->
		println("\nStream Prediction Output for ()()()()()()()()()()()()()()()()()()()()()()()()()()()() $t -> $c\n")
	}

//	cursor.close()
//	streamProxy.dispose() // TODO: It would be nice if it is possible to close all the cursor from the proxy, when disposing



	embeddedServer(Netty, 8080) {
		install(WebSockets)
		routing{
			webSocket ("/performance") {
				while(true){
					val currentPerformance = reportedPerformance; //TODO: Ideally, the next two line should be one "atomic" operation. Consider thread-safe mutable list.
					reportedPerformance = emptyList()
					if(currentPerformance.isNotEmpty()) {
						currentPerformance.forEach {
							outgoing.send(Frame.Text(it))
						}
					}
				}

			}
		}

	}.start(wait = true)



}
