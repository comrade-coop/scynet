package ai.scynet.launcher

import ai.scynet.protocol.StatusID
import ai.scynet.protocol.TRAINED
import ai.scynet.protocol.TrainingJob
import ai.scynet.queen.GattakkaLazyStream
import ai.scynet.queen.PredictingJobsStream
import ai.scynet.trainer.SelectedJobsStream
import ai.scynet.trainer.TrainingJobsStream
import com.fasterxml.jackson.databind.SerializationFeature
import descriptors.Properties
import harvester.candles.CandleDuration
import harvester.candles.CandleLazyStream
import harvester.datasets.DatasetStream
import harvester.exchanges.Exchange
import harvester.exchanges.XChangeLazyStream
import harvester.indicators.CompositeLengthIndicatorStream
import harvester.labels.CandleLabelStream
import harvester.pairs.PairingStream
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.apache.ignite.Ignite
import org.apache.ignite.services.Service
import org.apache.ignite.services.ServiceContext
import org.apache.logging.log4j.LogManager
import org.knowm.xchange.currency.CurrencyPair
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.nd4j.linalg.api.ndarray.INDArray
import processors.ILazyStream
import processors.ILazyStreamFactory
import processors.LazyStreamFactory
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class LauncherService : Service, KoinComponent {

    private val logger = LogManager.getLogger(this::class.qualifiedName)

    private val streamProxies: HashSet<ILazyStream> = HashSet()
    protected val ignite: Ignite by inject()

    override fun init(ctx: ServiceContext?) {

    }

    override fun cancel(ctx: ServiceContext?) {
        for(stream in streamProxies){
            stream.dispose()
        }
    }

    override fun execute(ctx: ServiceContext?) {
        val LAZY_STREAM_FACTORY = "lazyStreamFactory"
        ignite.services().deployClusterSingleton(LAZY_STREAM_FACTORY, LazyStreamFactory())
        val factory = ignite.services().serviceProxy(LAZY_STREAM_FACTORY, ILazyStreamFactory::class.java, false)

        val xChangeStreamId = UUID.randomUUID()
        val xChangeStream = XChangeLazyStream(xChangeStreamId, null, Properties().apply {
            put("currencyPair", CurrencyPair.ETH_USD)
            put("xchange", Exchange.BITMEX)
        })
        factory.registerStream(xChangeStream)

        val candleStreamId = UUID.randomUUID()
        val candleStream = CandleLazyStream(candleStreamId, ArrayList<UUID>().apply { add( xChangeStreamId)} , Properties().apply{
            put("candle", CandleDuration.MINUTE)
        })
        factory.registerStream(candleStream)

        val indicatorsId = UUID.randomUUID()
        val indicators = getIndicatorPeriodPairs(arrayOf("adx", "adxr", "ar", "dx","mdi","pdi","rsi","willr"))
        val indicatorsStream = CompositeLengthIndicatorStream(indicatorsId, candleStreamId, Properties().apply { put("indicators", indicators) })
        factory.registerStream(indicatorsStream)

        val labelStreamId = UUID.randomUUID()
        val labelProperties = Properties().apply {
            put("upperTresholdPercentage", 1.0)
            put("lowerTresholdPercentage", 0.5)
            put("periodInMinutes", 210)
        }
        val labelStream = CandleLabelStream(labelStreamId, candleStreamId,labelProperties)
        factory.registerStream(labelStream)

        val pairingStreamId = UUID.randomUUID()
        val pairingStream = PairingStream(pairingStreamId, arrayListOf(labelStreamId, indicatorsId))
        factory.registerStream(pairingStream)

        val datasetStreamId = UUID.randomUUID()
        val datasetStream = DatasetStream(datasetStreamId, pairingStreamId, Properties().apply {
            put("datasetSize", 2000)
            put("slide", 30)})
        factory.registerStream(datasetStream)

        // ------ Gattakka streams

        val gattakkaStreamID = UUID.randomUUID()
        val gattakkaStream = GattakkaLazyStream(gattakkaStreamID, null, Properties())
        factory.registerStream(gattakkaStream)

        val selectedJobsStreamID = UUID.randomUUID()
        val selectedJobsStream = SelectedJobsStream(selectedJobsStreamID, ArrayList<UUID>().apply { add(gattakkaStreamID) }, Properties())
        factory.registerStream(selectedJobsStream)

        val finishedJobsStreamID = UUID.randomUUID()
        val finishedJobsStream = TrainingJobsStream(finishedJobsStreamID, ArrayList<UUID>().apply { add(selectedJobsStreamID) }, Properties())
        factory.registerStream(finishedJobsStream)

        // ------ Dataset Listeners

        val datasetStreamProxy = factory.getInstance(datasetStreamId)
        streamProxies.add(datasetStreamProxy!!)
        datasetStreamProxy!!.listen{ datasetName: String, dataset: Pair<INDArray, INDArray>, _ ->
            // Listen operation implicitly start the service.
            println("\n\nDataset $datasetName ----> \n${dataset.first}   \n${dataset.second}\n\n")
        }

        // --- Business Logic Listeners

        var lastAgents = mutableListOf<Double>()

        var historyOfBestAccuracy = mutableListOf<Map<String, String>>()
        var bestAgentId: String = ""
        var bestAgentEgg: String = ""
        var bestAgentTrainingScore: Double = -1.0

        var bestAgentPredictionStream: PredictingJobsStream? = null
        var bestAgentLastPrediction: String = ""

        val performanceFeedbackCache = ignite.getOrCreateCache<String, Double>("tmp_perf")

        var finishedJobsStreamProxy = factory.getInstance(finishedJobsStreamID)
        streamProxies.add(finishedJobsStreamProxy!!)
        finishedJobsStreamProxy!!.listen { t:Long, c: TrainingJob, _ ->
            logger.info("\nStream Output for **************************************************************** $t -> $c\n")

            if(c.status.statusID == StatusID.TRAINED) {
                val perf = (c.status as TRAINED).results.getValue("performance")
                val perfDouble = if (perf.toDoubleOrNull() != null) perf.toDouble() else 0.0

                logger.info("Trained agent performance: ${perfDouble}")

                performanceFeedbackCache.put(c.UUID.toString(), perfDouble)

                if(lastAgents.count() > 10) {
                    lastAgents.removeAt(0)
                }
                bestAgentTrainingScore = if (lastAgents.count() > 0) lastAgents.max()!! else -1.0
                lastAgents.add(perfDouble)

                logger.info("Trained agent bestAgentTrainingScore: ${bestAgentTrainingScore}")

                if (perfDouble > bestAgentTrainingScore) {
                    bestAgentTrainingScore = perfDouble
                    bestAgentEgg = c.egg
                    bestAgentId = c.UUID.toString()
                    historyOfBestAccuracy.add(mapOf(
                            "t" to (System.currentTimeMillis() / 1000L).toString(),
                            "y" to bestAgentTrainingScore.toString()
                    ))
                    if (historyOfBestAccuracy.count() > 100) {
                        historyOfBestAccuracy.removeAt(0)
                    }

                    logger.info("Switch best agent to: ${bestAgentId}")

                    bestAgentPredictionStream?.dispose()
                    if(bestAgentPredictionStream != null){
                        streamProxies.remove(bestAgentPredictionStream!!)
                    }

                    val bestAgentStreamID = UUID.randomUUID()
                    bestAgentPredictionStream = PredictingJobsStream(bestAgentStreamID,
                            ArrayList<UUID>().apply { add(indicatorsId) },
                            Properties().apply {
                                put("id", bestAgentId)
                                put("egg", bestAgentEgg)
                            })
                    factory.registerStream(bestAgentPredictionStream!!)

                    var bestAgentStreamProxy = factory.getInstance(bestAgentStreamID)
                    streamProxies.add(bestAgentStreamProxy!!)

                    bestAgentStreamProxy!!.listen { _: Long, prediction: String, _ ->
                        bestAgentLastPrediction = prediction
                    }
                }
            }
        }

//	cursor.close() // TODO: May be?
//	streamProxy.dispose() // TODO: It would be nice if it is possible to close all the cursor from the proxy, when disposing

        embeddedServer(Netty, 8080) {
            install(ContentNegotiation) {
                jackson {
                    enable(SerializationFeature.INDENT_OUTPUT) // Pretty Prints the JSON
                }
            }
            install(CORS)
            {
                anyHost()
            }
            routing {
                get("/test") {
                    call.respond(mapOf("OK" to true))
                }

                get("/all") {
                    call.respond(mapOf(
                            "bestAgentId" to bestAgentId,
                            "bestAgentTrainingScore" to bestAgentTrainingScore,
                            "bestAgentLastPrediction" to bestAgentLastPrediction,
                            "historyOfBestAccuracy" to historyOfBestAccuracy
                    ))
                }

                get("/agent") {
                    call.respond(mapOf(
                            "bestAgentId" to bestAgentId,
                            "bestAgentTrainingScore" to bestAgentTrainingScore,
                            "bestAgentLastPrediction" to bestAgentLastPrediction
                    ))
                }

                get("/history") {
                    call.respond(mapOf(
                            "historyOfBestAccuracy" to historyOfBestAccuracy
                    ))
                }
            }
        }.start(wait = true)
    }

    private fun getIndicatorPeriodPairs(indicators: Array<String>): ArrayList<Pair<String, Int>>{
        val periods = arrayListOf(6,10,20,30,40,50,70,100,150,200)
        val indicatorPeriodPairs = ArrayList<Pair<String, Int>>()
        for(indicator in indicators){
            for (period in periods){
                indicatorPeriodPairs.add(Pair(indicator, period))
            }
        }
        return indicatorPeriodPairs
    }
}