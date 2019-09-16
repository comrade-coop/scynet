package harvester

import descriptors.Properties
import harvester.candles.CandleCombinerStream
import harvester.candles.CandleDuration
import harvester.candles.CandleLazyStream
import harvester.combiners.INDArrayCombinerStream
import harvester.datasets.DatasetStream
import harvester.exchanges.Exchange
import harvester.exchanges.TickerSaver
import harvester.exchanges.XChangeLazyStream
import harvester.indicators.CompositeLengthIndicatorStream
import harvester.labels.CandleLabelStream
import harvester.normalization.NormalizingStream
import harvester.pairs.PairingStream
import harvester.windows.WindowingStream
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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

    GlobalScope.launch {
        val tickerWriter = TickerSaver(CurrencyPair.ETH_USD)

        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                tickerWriter.stop()
            }
        })
        GlobalScope.launch {
            while(readLine() != "stop"){
                println("Only stop command accepted!")
            }
            exitProcess(0)
        }

        tickerWriter.start()
    }
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
    val windowingStream = WindowingStream(windowingStreamId, candleCombinerId, Properties().apply { put("windowSize", 10) })

    val normalizingStreamId = UUID.randomUUID()
    val normalizingStream = NormalizingStream(normalizingStreamId, windowingStreamId)

    val indicatorsId = UUID.randomUUID()
    val indicators = getIndicatorPeriodPairs(arrayOf("adx", "adxr", "sma", "sma", "ar", "dx","mdi","pdi","rsi","willr"))
    val indicatorsStream = CompositeLengthIndicatorStream(indicatorsId, candleStreamId, Properties().apply { put("indicators", indicators) })

    val windowedIndicatorsStreamId = UUID.randomUUID()
    val windowedIndicatorsStream = WindowingStream(windowedIndicatorsStreamId, indicatorsId, Properties().apply { put("windowSize", 10) })

    val iNDCombinerId = UUID.randomUUID()
    val iNDcombinerStream = INDArrayCombinerStream(iNDCombinerId, arrayListOf(normalizingStreamId, windowedIndicatorsStreamId))


    val labelStreamId = UUID.randomUUID()
    val labelProperties = Properties().apply {
        put("upperTresholdPercentage", 1.0)
        put("lowerTresholdPercentage", 0.5)
        put("periodInMinutes", 30)
    }
    val labelStream = CandleLabelStream(labelStreamId, candleStreamId,labelProperties)

    val pairingStreamId = UUID.randomUUID()
    val pairingStream = PairingStream(pairingStreamId, arrayListOf(labelStreamId, normalizingStreamId))

    val datasetStreamId = UUID.randomUUID()
    val datasetStream = DatasetStream(datasetStreamId, pairingStreamId, Properties().apply {
        put("datasetSize", 50)
        put("slide", 25)})

    //Register streams
    val LAZY_STREAM_FACTORY = "lazyStreamFactory"
    ignite.services().deployClusterSingleton(LAZY_STREAM_FACTORY, LazyStreamFactory())
    val factory = ignite.services().serviceProxy(LAZY_STREAM_FACTORY, ILazyStreamFactory::class.java, false)
    factory.registerStream(xChangeStream)
    factory.registerStream(candleStream)
    factory.registerStream(xChangeStream2)
    factory.registerStream(candleStream2)
    factory.registerStream(candleCombinerStream)
    factory.registerStream(indicatorsStream)
    factory.registerStream(iNDcombinerStream)
    factory.registerStream(windowingStream)
    factory.registerStream(normalizingStream)
    factory.registerStream(labelStream)
    factory.registerStream(pairingStream)
    factory.registerStream(datasetStream)
    factory.registerStream(windowedIndicatorsStream)

    val datasetStreamProxy = factory.getInstance(datasetStreamId)
    val cursor = datasetStreamProxy.listen{ datasetName: String, dataset: Pair<INDArray, INDArray>, _ ->
        println("\n\nDataset $datasetName ----> \n${dataset.first}   \n${dataset.second}\n\n")
    }

    Thread.sleep(1440000)
    cursor.close()
    datasetStreamProxy.dispose()
    exitProcess(0)
}

fun getIndicatorPeriodPairs(indicators: Array<String>): ArrayList<Pair<String, Int>>{
    val periods = arrayListOf(5,10,20,25,30,35,40,50,75,100)
    val indicatorPeriodPairs = ArrayList<Pair<String, Int>>()
    for(indicator in indicators){
        for (period in periods){
            indicatorPeriodPairs.add(Pair(indicator, period))
        }
    }
    return indicatorPeriodPairs
}