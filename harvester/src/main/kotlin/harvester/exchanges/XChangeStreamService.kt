package harvester.exchanges

import info.bitrich.xchangestream.core.ProductSubscription
import info.bitrich.xchangestream.core.StreamingExchange
import info.bitrich.xchangestream.core.StreamingExchangeFactory
import io.reactivex.disposables.Disposable
import org.apache.ignite.cache.affinity.AffinityKey
import org.apache.ignite.services.ServiceContext
import org.knowm.xchange.currency.CurrencyPair
import org.knowm.xchange.dto.marketdata.Ticker
import processors.LazyStreamService
import java.io.BufferedReader
import java.io.FileReader
import java.lang.IllegalStateException
import java.math.BigDecimal
import java.time.Instant
import java.util.*

class XChangeStreamService: LazyStreamService<Long, Ticker>() {
    private lateinit var exchange : IExchange
    private lateinit var currencyPair : CurrencyPair
    private var fromFile: Boolean? = null

    private lateinit var xchange: StreamingExchange
    private lateinit var xChangeStream: Disposable

    inner class TickerReader(private val fileName: String) {
        private val bidRegex = Regex("bid=[0-9.]*")
        private val askRegex = Regex("ask=[0-9.]*")
        private val timestampRegex = Regex("timestamp=[0-9]*")
        fun start(){
            val reader = BufferedReader(FileReader(fileName))
            reader.use {
                var line = it.readLine()
                while (line != null){
                    parseTicker(line)
                    line = it.readLine()
                }
            }
        }

        private fun parseTicker(line: String){
            val bid = BigDecimal( bidRegex.find(line)!!.value.split('=').get(1))
            val ask = BigDecimal(askRegex.find(line)!!.value.split('=').get(1))
            val timestamp =Date.from(Instant.ofEpochMilli((timestampRegex.find(line)!!.value.split('=').get(1)).toLong()))

            val ticker = Ticker.Builder().apply {
                bid(bid)
                ask(ask)
                timestamp(timestamp)
            }.build()
            streamTicker(ticker)
        }
    }

    override fun init(ctx: ServiceContext?) {
        super.init(ctx)
        exchange = descriptor!!.properties!!.get("xchange") as IExchange
        currencyPair = descriptor!!.properties!!.get("currencyPair") as CurrencyPair
        fromFile = descriptor!!.properties!!.get("fromFile") as Boolean
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

        if(fromFile!!){
            readTickersFromFile()
        }

        println("\nnew tickers from exchange\n")
        xChangeStream = xchange.streamingMarketDataService.getTicker(currencyPair).subscribe { ticker ->
            streamTicker(ticker)
        }
    }

    override fun cancel(ctx: ServiceContext?) {
        xChangeStream.dispose()
        println("xChangeStream disposed!")
        xchange.disconnect()
        println("xchange disconnected!")
        super.cancel(ctx)
    }

    private fun readTickersFromFile(){
        val splittedCurrencyPair = currencyPair.toString().split('/')
        val tickerReader = TickerReader("tickers-${exchange}-${splittedCurrencyPair[0]}-${splittedCurrencyPair[1]}")
        tickerReader.start()
    }

    private fun streamTicker(ticker: Ticker){
        var timestamp = 0L
        try{
            timestamp = ticker.timestamp.time
            cache.put(timestamp, ticker)
        }catch (e: IllegalStateException){
            val time = Instant.now()
            timestamp = time.toEpochMilli()
            val newTicker = Ticker.Builder().apply {
                currencyPair(ticker.currencyPair)
                open(ticker.open)
                last(ticker.last)
                bid(ticker.bid)
                ask(ticker.ask)
                high(ticker.high)
                low(ticker.low)
                volume(ticker.volume)
                quoteVolume(ticker.quoteVolume)
                bidSize(ticker.bidSize)
                askSize(ticker.askSize)
                timestamp(Date.from(time))
            }.build()
            cache.put(timestamp, newTicker)
        }
    }
}