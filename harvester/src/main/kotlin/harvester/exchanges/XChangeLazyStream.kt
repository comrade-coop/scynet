package harvester.exchanges

import org.knowm.xchange.currency.CurrencyPair
import org.knowm.xchange.dto.marketdata.Ticker
import processors.ILazyStreamService
import processors.LazyStream
import java.util.*

val xChangeServiceProperties = Properties().apply {
    put("currencyPair", CurrencyPair.BTC_USD)
    put("xchange", Exchange.COINBASE_PRO)
}

class XChangeLazyStream(id: UUID): LazyStream<Ticker>(id){
    override val streamService: ILazyStreamService<Ticker> = XChangeStreamService(xChangeServiceProperties)
}