package harvester

import harvester.candles.Candle
import harvester.candles.CandleLazyStream
import harvester.exchanges.Exchange
import harvester.exchanges.XChangeLazyStream
import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.apache.ignite.configuration.IgniteConfiguration
import org.knowm.xchange.currency.CurrencyPair
import org.knowm.xchange.dto.marketdata.Ticker
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.util.*

fun main(){
    val cfg = IgniteConfiguration()
    cfg.setIgniteInstanceName("HarvesterTest")
    val ignite = Ignition.start(cfg)

    startKoin {
        printLogger()
        modules(module {
            single<Ignite> { ignite }
        })
    }

    val xChangeServiceProperties = Properties().apply {
        put("currencyPair", CurrencyPair.BTC_USD)
        put("xchange", Exchange.COINBASE_PRO)
    }

    val candleServiceProperties = Properties().apply {
        put("candle", Candle.MINUTE)
    }
    val lazyCandleStream = CandleLazyStream(XChangeLazyStream(UUID.randomUUID(), xChangeServiceProperties),candleServiceProperties)

    var cursor = lazyCandleStream.listen{ k: Long, v: Ticker, _ ->
        println("Stream Output for $k  -->  $v")
    }
    Thread.sleep(300000)
    cursor.close()
    println("\n Disengaging Candle Stream \n")
    lazyCandleStream.disengageStream()

}