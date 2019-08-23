package harvester

import ai.scynet.core.descriptors.StreamDescriptor
import descriptors.LazyStreamDescriptor
import descriptors.LazyStreamServiceDescriptor
import harvester.candles.Candle
import harvester.candles.CandleDTO
import harvester.candles.CandleLazyStream
import harvester.candles.CandleStreamService
import harvester.exchanges.Exchange
import harvester.exchanges.XChangeLazyStream
import harvester.exchanges.XChangeStreamService
import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.services.ServiceDescriptor
import org.knowm.xchange.currency.CurrencyPair
import org.koin.core.context.startKoin
import org.koin.dsl.module
import processors.ILazyStreamFactory
import processors.LazyStreamFactory
import java.util.*
import kotlin.collections.HashMap
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
    val xChangeStreamDescriptor = LazyStreamDescriptor(
            id = xChangeStreamId,
            streamClass = XChangeLazyStream::class,
            serviceDescriptor = LazyStreamServiceDescriptor(
                    streamServiceClass = XChangeStreamService::class,
                    inputStreamId = UUID.randomUUID(),
                    properties = HashMap<String, Any>().apply {
                        put("currencyPair", CurrencyPair.BTC_USD)
                        put("xchange", Exchange.COINBASE_PRO)
                    }
            )
    )


    val candleStreamId = UUID.randomUUID()
    println("\ncandleStreamId -> $candleStreamId\n")
    val candleStreamDescriptor = LazyStreamDescriptor(
            id = candleStreamId,
            streamClass = CandleLazyStream::class,
            serviceDescriptor = LazyStreamServiceDescriptor(
                    streamServiceClass = CandleStreamService::class,
                    inputStreamId = xChangeStreamId,
                    properties = HashMap<String, Any>().apply{
                        put("candle", Candle.MINUTE)
                    }
            )
    )

    //Register streams
    val factory = ignite.services().serviceProxy(LAZY_STREAM_FACTORY, ILazyStreamFactory::class.java, false)
    factory.registerStream(xChangeStreamDescriptor)
    factory.registerStream(candleStreamDescriptor)

    //Listen to candleStream
    var candleStream = factory.getInstance(candleStreamId)
    var cursor =  candleStream.listen {t:Long , c: CandleDTO, _ ->
        println("\nStream Output for $t -> $c\n")
    }
    Thread.sleep(180000)

    cursor.close()
    candleStream.dispose()
    Thread.sleep(30000)

    println("\nRestarting Candle Stream!\n")
    candleStream = factory.getInstance(candleStreamId)
    cursor =  candleStream.listen {t:Long , c: CandleDTO, _ ->
        println("\nStream Output for $t -> $c\n")
    }
    Thread.sleep(180000)

    cursor.close()
    candleStream.dispose()
    Thread.sleep(30000)

    exitProcess(0)
}