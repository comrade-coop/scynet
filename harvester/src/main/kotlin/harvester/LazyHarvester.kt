package harvester

import descriptors.Properties
import harvester.candles.Candle
import harvester.candles.CandleDTO
import harvester.candles.CandleLazyStream
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
    val xChangeStream = XChangeLazyStream(xChangeStreamId, UUID.randomUUID(), Properties().apply {
        put("currencyPair", CurrencyPair.BTC_USD)
        put("xchange", Exchange.COINBASE_PRO)
    })


    val candleStreamId = UUID.randomUUID()
    println("\ncandleStreamId -> $candleStreamId\n")
    val candleStream = CandleLazyStream(candleStreamId, xChangeStreamId,Properties().apply{
        put("candle", Candle.MINUTE)
    } )


    //Register streams
    val factory = ignite.services().serviceProxy(LAZY_STREAM_FACTORY, ILazyStreamFactory::class.java, false)
    factory.registerStream(xChangeStream)
    factory.registerStream(candleStream)


    //Listen to candleStream
    var candleStreamProxy = factory.getInstance(candleStreamId)
    var cursor =  candleStreamProxy.listen { t:Long, c: CandleDTO, _ ->
        println("\nStream Output for $t -> $c\n")
    }
    Thread.sleep(180000)

    cursor.close()
    candleStreamProxy.dispose()
    Thread.sleep(30000)

    println("\nRestarting Candle Stream!\n")
    candleStreamProxy = factory.getInstance(candleStreamId)
    cursor =  candleStreamProxy.listen { t:Long, c: CandleDTO, _ ->
        println("\nStream Output for $t -> $c\n")
    }
    Thread.sleep(180000)

    cursor.close()
    candleStreamProxy.dispose()
    Thread.sleep(30000)

    exitProcess(0)
}