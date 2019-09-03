package ai.scynet.harvester

import ai.scynet.common.registry.Registry
import ai.scynet.core.common.registry.IgniteRegistry
import ai.scynet.core.configurations.ConfigurationBase
import ai.scynet.core.configurations.ProcessorConfiguration
import ai.scynet.core.processors.ProcessorFactory
import ai.scynet.core.processors.Stream
import harvester.candles.Candle
import harvester.candles.CandleProcessor
import harvester.exchanges.Exchange
import harvester.exchanges.XChangeHarvester
import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.knowm.xchange.currency.CurrencyPair
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import java.util.*

fun main(args: Array<String>) {
    startKoin {
        printLogger()
        modules(module {
            single<Ignite> { Ignition.start() }
            single<IgniteRegistry<String, Stream>>(named("streamRegistry")) { IgniteRegistry("streamRegistry")
            } bind Registry::class
        })
    }
    val processorFactory = ProcessorFactory()
    val harvesterConfig: ProcessorConfiguration = ConfigurationBase().processors {
        processor {
            problem = "crypto trading"
            processorClass = XChangeHarvester::class
            inputs = mutableListOf()
            properties = Properties().apply {
                put("xchange", Exchange.COINBASE_PRO)
                put("currencyPair", CurrencyPair.BTC_USD)
            }
        }
    }[0]
    val harvester = processorFactory.create(harvesterConfig)
    harvester.start()

    Thread.sleep(10000L)

    val candleProcessorConfig = ConfigurationBase().processors {
        processor{
                problem = "crypto trading"
                processorClass = CandleProcessor::class
                inputs = mutableListOf("COINBASE_PRO-BTC/USD-TickerHarvester")
                properties = Properties().apply {
                    put("candle", Candle.MINUTE)
                }
        }
    }[0]
    val candleProcessor = processorFactory.create(candleProcessorConfig)
    candleProcessor.start()
    
    Thread.sleep(180000)

    harvester.stop()
    candleProcessor.stop()
    System.exit(0)
}
