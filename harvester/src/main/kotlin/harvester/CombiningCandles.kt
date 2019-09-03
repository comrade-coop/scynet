package harvester

import descriptors.Properties
import harvester.candles.*
import harvester.exchanges.Exchange
import harvester.exchanges.XChangeLazyStream
import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.apache.ignite.configuration.IgniteConfiguration
import org.knowm.xchange.currency.CurrencyPair
import org.koin.core.context.startKoin
import org.koin.dsl.module
import processors.ILazyStreamFactory
import processors.LazyStreamFactory
import java.util.*
import kotlin.collections.ArrayList
import kotlinx.coroutines.*
import kotlin.concurrent.thread

import kotlin.system.exitProcess

fun main(){

    val cfg = IgniteConfiguration()
    cfg.igniteInstanceName = "HarvesterTest"
    val ignite = Ignition.start(cfg)

    startKoin {
        printLogger()
        modules(module {
            single<Ignite> { ignite }
        })
    }
    val LAZY_STREAM_FACTORY = "lazyStreamFactory"

    ignite.services().deployClusterSingleton(LAZY_STREAM_FACTORY, LazyStreamFactory())

    val xChangeStreamId = UUID.randomUUID()
    println("\nxChangeStreamId -> $xChangeStreamId\n")
    val xChangeStream = XChangeLazyStream(xChangeStreamId, null, Properties().apply {
        put("currencyPair", CurrencyPair.BTC_USD)
        put("xchange", Exchange.COINBASE_PRO)
    })

    val candleStreamId = UUID.randomUUID()
    println("\ncandleStreamId -> $candleStreamId\n")
    val candleStream = CandleLazyStream(candleStreamId, ArrayList<UUID>().apply { add( xChangeStreamId)} ,Properties().apply{
        put("candle", Candle.MINUTE)
    } )

    val xChangeStreamId2 = UUID.randomUUID()
    println("\nxChangeStreamId -> $xChangeStreamId2\n")
    val xChangeStream2 = XChangeLazyStream(xChangeStreamId2, null, Properties().apply {
        put("currencyPair", CurrencyPair.BTC_USD)
        put("xchange", Exchange.COINBASE_PRO)
    })

    val candleStreamId2 = UUID.randomUUID()
    println("\ncandleStreamId -> $candleStreamId2\n")
    val candleStream2 = CandleLazyStream(candleStreamId2, ArrayList<UUID>().apply { add( xChangeStreamId2)} ,Properties().apply{
        put("candle", Candle.MINUTE)
    } )


    val candleCombinerId = UUID.randomUUID()
    val candleCombinerStream = CandleCombinerStream(candleCombinerId, ArrayList<UUID>().apply{
        add(candleStreamId)
        add(candleStreamId2)
    })

    //Register streams
    val factory = ignite.services().serviceProxy(LAZY_STREAM_FACTORY, ILazyStreamFactory::class.java, false)
    factory.registerStream(xChangeStream)
    factory.registerStream(candleStream)
    factory.registerStream(xChangeStream2)
    factory.registerStream(candleStream2)
    factory.registerStream(candleCombinerStream)

    val candleCombinerProxy = factory.getInstance(candleCombinerId)
    val cursor = candleCombinerProxy.listen{ timestamp: Long, combined: Pair<CandleDTO, CandleDTO>,_ ->
        println("\nCombined Output ----> $combined\n")
    }
    Thread.sleep(720000)
    exitProcess(0)
}