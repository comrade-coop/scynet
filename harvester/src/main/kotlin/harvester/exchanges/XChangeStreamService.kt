package harvester.exchanges

import info.bitrich.xchangestream.core.ProductSubscription
import info.bitrich.xchangestream.core.StreamingExchange
import info.bitrich.xchangestream.core.StreamingExchangeFactory
import io.reactivex.disposables.Disposable
import org.apache.ignite.services.ServiceContext
import org.knowm.xchange.currency.CurrencyPair
import org.knowm.xchange.dto.marketdata.Ticker
import processors.LazyStreamService
import java.util.*

class XChangeStreamService: LazyStreamService<Ticker>() {

    private lateinit var exchange : IExchange
    private lateinit var currencyPair : CurrencyPair

    private lateinit var xchange: StreamingExchange
    private lateinit var xChangeStream: Disposable

    override fun init(ctx: ServiceContext?) {
        super.init(ctx)
        exchange = descriptor!!.properties.get("xchange") as IExchange
        currencyPair = descriptor!!.properties.get("currencyPair") as CurrencyPair
    }
    override fun execute(ctx: ServiceContext?) {
        super.execute(ctx)

        xchange = StreamingExchangeFactory.INSTANCE.createExchange(exchange.getExchangeClassName())
        //Some xchanges need  ProductSubscription
        val productSubscription = ProductSubscription
                .create()
                .addTicker(currencyPair)
                .build()

        xchange.connect(productSubscription).blockingAwait()
        println("Exchange connected")
        xChangeStream = xchange.streamingMarketDataService.getTicker(currencyPair).subscribe { ticker ->
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