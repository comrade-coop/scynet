package harvester

import descriptors.Properties
import harvester.candles.CandleDuration
import harvester.candles.CandleLazyStream
import harvester.datasets.DatasetStream
import harvester.exchanges.Exchange
import harvester.exchanges.TickerSaver
import harvester.exchanges.XChangeLazyStream
import harvester.indicators.CompositeLengthIndicatorStream
import harvester.labels.CandleLabelStream
import harvester.pairs.PairingStream
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
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
    })

    val candleStreamId = UUID.randomUUID()
    val candleStream = CandleLazyStream(candleStreamId, ArrayList<UUID>().apply { add( xChangeStreamId)} , Properties().apply{
        put("candle", CandleDuration.MINUTE)
    } )


    val indicatorsId = UUID.randomUUID()
    val indicators = getIndicatorPeriodPairs(arrayOf("adx", "adxr", "ar", "dx","mdi","pdi","rsi","willr"))
    val indicatorsStream = CompositeLengthIndicatorStream(indicatorsId, candleStreamId, Properties().apply { put("indicators", indicators) })


    val labelStreamId = UUID.randomUUID()
    val labelProperties = Properties().apply {
        put("upperTresholdPercentage", 1.0)
        put("lowerTresholdPercentage", 0.5)
        put("periodInMinutes", 210)
    }
    val labelStream = CandleLabelStream(labelStreamId, candleStreamId,labelProperties)

    val pairingStreamId = UUID.randomUUID()
    val pairingStream = PairingStream(pairingStreamId, arrayListOf(labelStreamId, indicatorsId))

    val datasetStreamId = UUID.randomUUID()
    val datasetStream = DatasetStream(datasetStreamId, pairingStreamId, Properties().apply {
        put("datasetSize", 2000)
        put("slide", 30)})

    //Register streams
    val LAZY_STREAM_FACTORY = "lazyStreamFactory"
    ignite.services().deployClusterSingleton(LAZY_STREAM_FACTORY, LazyStreamFactory())
    val factory = ignite.services().serviceProxy(LAZY_STREAM_FACTORY, ILazyStreamFactory::class.java, false)
    factory.registerStream(xChangeStream)
    factory.registerStream(candleStream)
    factory.registerStream(indicatorsStream)
    factory.registerStream(labelStream)
    factory.registerStream(pairingStream)
    factory.registerStream(datasetStream)

    val datasetStreamProxy = factory.getInstance(datasetStreamId)
    val cursor = datasetStreamProxy!!.listen{ datasetName: String, dataset: Pair<INDArray, INDArray>, _ ->
        println("\n\nDataset $datasetName ----> \n${dataset.first}   \n${dataset.second}\n\n")
    }


    GlobalScope.launch {
        val tickerWriter = TickerSaver(CurrencyPair.ETH_USD)

        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                tickerWriter.stop()
            }
        })
        launch {
            while(readLine() != "stop"){
                println("Only stop command accepted!")
            }
            cursor.close()
            datasetStreamProxy.dispose()
            while(true){
                val serviceDescriptors = ignite.services().serviceDescriptors()
                if(serviceDescriptors!!.size != 1){
                    delay(10000)
                }else{
                    ignite.services().cancelAll()
                    exitProcess(0)
                }
            }
        }

        tickerWriter.start()
    }

    while(true){}
}

fun getIndicatorPeriodPairs(indicators: Array<String>): ArrayList<Pair<String, Int>>{
    val periods = arrayListOf(6,10,20,30,40,50,70,100,150,200)
    val indicatorPeriodPairs = ArrayList<Pair<String, Int>>()
    for(indicator in indicators){
        for (period in periods){
            indicatorPeriodPairs.add(Pair(indicator, period))
        }
    }
    return indicatorPeriodPairs
}