package harvester

import descriptors.Properties
import harvester.candles.CandleDuration
import harvester.candles.CandleLazyStream
import harvester.exchanges.Exchange
import harvester.exchanges.XChangeLazyStream
import harvester.indicators.SimpleMovingAverageStream
import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.apache.ignite.configuration.IgniteConfiguration
import org.knowm.xchange.currency.CurrencyPair
import org.koin.core.context.startKoin
import org.koin.dsl.module
import processors.ILazyStreamFactory
import processors.LazyStreamFactory
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList

fun main(){
    val cfg = IgniteConfiguration()
    cfg.igniteInstanceName = "TechnicalIndicatorsTest"
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

    val smaId = UUID.randomUUID()
    val smaStream = SimpleMovingAverageStream(smaId, candleStreamId, Properties().apply { put("averagingPeriod", 30) })

    //Register streams
    val LAZY_STREAM_FACTORY = "lazyStreamFactory"
    ignite.services().deployClusterSingleton(LAZY_STREAM_FACTORY, LazyStreamFactory())
    val factory = ignite.services().serviceProxy(LAZY_STREAM_FACTORY, ILazyStreamFactory::class.java, false)
    factory.registerStream(xChangeStream)
    factory.registerStream(candleStream)
    factory.registerStream(smaStream)


    val smaProxy = factory.getInstance(smaId)
    val cursor = smaProxy.listen{ timestamp: Long, sma: Double, _ ->
        println("SMA at ${Date.from(Instant.ofEpochMilli(timestamp))} ---> $sma")
    }

    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            cursor.close()
            smaProxy.dispose()
            println("Resources successfully released!")
        }
    })

    Thread.sleep(720000)
}