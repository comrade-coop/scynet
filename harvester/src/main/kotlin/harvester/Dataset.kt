package harvester

import descriptors.Properties
import harvester.candles.CandleCombinerStream
import harvester.candles.CandleDuration
import harvester.candles.CandleLazyStream
import harvester.datasets.DatasetStream
import harvester.exchanges.Exchange
import harvester.exchanges.XChangeLazyStream
import harvester.labels.CandleLabelStream
import harvester.normalization.NormalizingStream
import harvester.pairs.PairingStream
import harvester.windows.WindowingStream
import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.apache.ignite.configuration.IgniteConfiguration
import org.knowm.xchange.currency.CurrencyPair
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.nd4j.linalg.api.ndarray.INDArray
import processors.ILazyStreamFactory
import processors.LazyStreamFactory
import java.util.*
import kotlin.collections.ArrayList
import kotlin.system.exitProcess

fun main(){
    val cfg = IgniteConfiguration()
    cfg.igniteInstanceName = "DatasetTest"
    val ignite = Ignition.start(cfg)

    startKoin {
        printLogger()
        modules(module {
            single<Ignite> { ignite }
        })
    }

    val xChangeStreamId = UUID.randomUUID()
    val xChangeStream = XChangeLazyStream(xChangeStreamId, null, Properties().apply {
        put("currencyPair", CurrencyPair.ETH_USD)
        put("xchange", Exchange.BITMEX)
        put("fromFile", true)
    })

    val candleStreamId = UUID.randomUUID()
    val candleStream = CandleLazyStream(candleStreamId, ArrayList<UUID>().apply { add( xChangeStreamId)} , Properties().apply{
        put("candle", CandleDuration.MINUTE)
    } )

    val xChangeStreamId2 = UUID.randomUUID()
    val xChangeStream2 = XChangeLazyStream(xChangeStreamId2, null, Properties().apply {
        put("currencyPair", CurrencyPair.ETH_USD)
        put("xchange", Exchange.COINBASE_PRO)
        put("fromFile", true)
    })

    val candleStreamId2 = UUID.randomUUID()
    val candleStream2 = CandleLazyStream(candleStreamId2, ArrayList<UUID>().apply { add( xChangeStreamId2)} , Properties().apply{
        put("candle", CandleDuration.MINUTE)
    } )


    val candleCombinerId = UUID.randomUUID()
    val candleCombinerStream = CandleCombinerStream(candleCombinerId, ArrayList<UUID>().apply{
        add(candleStreamId)
        add(candleStreamId2)
    })


    val windowingStreamId = UUID.randomUUID()
    val windowingStream = WindowingStream(windowingStreamId, candleCombinerId, Properties().apply { put("windowSize", 2) })

    val normalizingStreamId = UUID.randomUUID()
    val normalizingStream = NormalizingStream(normalizingStreamId, windowingStreamId)

    val labelStreamId = UUID.randomUUID()
    val labelProperties = Properties().apply {
        put("upperTresholdPercentage", 10.0)
        put("lowerTresholdPercentage", 5.0)
        put("periodInMinutes", 2)
    }
    val labelStream = CandleLabelStream(labelStreamId, candleStreamId,labelProperties)

    val pairingStreamId = UUID.randomUUID()
    val pairingStream = PairingStream(pairingStreamId, arrayListOf(labelStreamId, normalizingStreamId))

    val datasetStreamId = UUID.randomUUID()
    val datasetStream = DatasetStream(datasetStreamId, pairingStreamId, Properties().apply { put("datasetSize", 10) })

    //Register streams
    val LAZY_STREAM_FACTORY = "lazyStreamFactory"
    ignite.services().deployClusterSingleton(LAZY_STREAM_FACTORY, LazyStreamFactory())
    val factory = ignite.services().serviceProxy(LAZY_STREAM_FACTORY, ILazyStreamFactory::class.java, false)
    factory.registerStream(xChangeStream)
    factory.registerStream(candleStream)
    factory.registerStream(xChangeStream2)
    factory.registerStream(candleStream2)
    factory.registerStream(candleCombinerStream)
    factory.registerStream(windowingStream)
    factory.registerStream(normalizingStream)
    factory.registerStream(labelStream)
    factory.registerStream(pairingStream)
    factory.registerStream(datasetStream)

    val datasetStreamProxy = factory.getInstance(datasetStreamId)
    val cursor = datasetStreamProxy.listen{ datasetName: String, dataset: Pair<INDArray, INDArray>, _ ->
        println("\n\nDataset $datasetName ----> \n${dataset.first}   \n${dataset.second}\n\n")
    }

    Thread.sleep(1440000)
    cursor.close()
    datasetStreamProxy.dispose()
    exitProcess(0)
}