package harvester

import harvester.exchanges.Exchange
import harvester.exchanges.XChangeStreamService
import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.apache.ignite.Ignition.ignite
import org.apache.ignite.configuration.IgniteConfiguration
import org.knowm.xchange.currency.CurrencyPair
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

    val harvesterProperties = Properties().apply {
        put("currencyPair", CurrencyPair.BTC_USD)
        put("xchange", Exchange.COINBASE_PRO)
    }
    val continuousHarvester = XChangeStreamService(harvesterProperties)
    val services = ignite("HarvesterTest").services()
    services.deployClusterSingleton("XchangeHarvester", continuousHarvester)

}