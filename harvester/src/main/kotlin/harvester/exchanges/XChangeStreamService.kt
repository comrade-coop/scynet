package harvester.exchanges

import info.bitrich.xchangestream.core.ProductSubscription
import info.bitrich.xchangestream.core.StreamingExchange
import info.bitrich.xchangestream.core.StreamingExchangeFactory
import io.reactivex.disposables.Disposable
import org.apache.ignite.services.ServiceContext
import org.knowm.xchange.currency.CurrencyPair
import org.knowm.xchange.dto.marketdata.Ticker
import processors.ContinuousStreamService
import java.util.*

class XChangeStreamService(properties: Properties): ContinuousStreamService<Ticker>(30) {

    private val exchange = properties.get("xchange") as IExchange
    private val currencyPair = properties.get("currencyPair") as CurrencyPair

    private lateinit var xchange: StreamingExchange
    private lateinit var xChangeStream: Disposable

    override fun execute(ctx: ServiceContext?) {
        xchange = StreamingExchangeFactory.INSTANCE.createExchange(exchange.getExchangeClassName())
        println("Starting $serviceName")
        //Some xchanges need  ProductSubscription
        val productSubscription = ProductSubscription
                .create()
                .addTicker(currencyPair)
                .build()

        xchange.connect(productSubscription).blockingAwait()
        println("Exchange connected")
        xChangeStream = xchange.streamingMarketDataService.getTicker(currencyPair).subscribe {
            ticker -> println(ticker)
            cache.put(ticker.timestamp.time, ticker)
        }
    }

    override fun cancel(ctx: ServiceContext?) {
        xChangeStream.dispose()
        println("xChangeStream disposed!")
        xchange.disconnect()
        println("xchange disconnected!")
        super.cancel(ctx)
    }
}