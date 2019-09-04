package harvester.exchanges

import info.bitrich.xchangestream.binance.BinanceStreamingExchange
import info.bitrich.xchangestream.bitfinex.BitfinexStreamingExchange
import info.bitrich.xchangestream.bitflyer.BitflyerStreamingExchange
import info.bitrich.xchangestream.bitmex.BitmexStreamingExchange
import info.bitrich.xchangestream.coinbasepro.CoinbaseProStreamingExchange
import info.bitrich.xchangestream.okcoin.OkCoinStreamingExchange
import info.bitrich.xchangestream.okcoin.OkExStreamingExchange
import info.bitrich.xchangestream.poloniex.PoloniexStreamingExchange

enum class Exchange private constructor(private val className: String): IExchange {
    BINANCE(BinanceStreamingExchange::class.java.name),
    BITFINEX(BitfinexStreamingExchange::class.java.name),
    BITFLYER(BitflyerStreamingExchange::class.java.name),
    BITMEX(BitmexStreamingExchange::class.java.name),
    COINBASE_PRO(CoinbaseProStreamingExchange::class.java.name),
    OKCOIN(OkCoinStreamingExchange::class.java.name),
    OKEX(OkExStreamingExchange::class.java.name),
    POLONIEX(PoloniexStreamingExchange::class.java.name);

    override fun getExchangeClassName(): String{
        return className
    }
}