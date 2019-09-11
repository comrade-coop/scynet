package harvester.exchanges

import info.bitrich.xchangestream.core.ProductSubscription
import info.bitrich.xchangestream.core.StreamingExchangeFactory
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.*
import org.knowm.xchange.currency.CurrencyPair
import java.io.BufferedWriter
import java.io.FileWriter
import java.util.concurrent.ConcurrentHashMap

class TickerSaver(private val currencyPair: CurrencyPair) {
    private val splittedCurrencyPair = currencyPair.toString().split('/')
    private val writersMap: ConcurrentHashMap<String, BufferedWriter> = ConcurrentHashMap()
    private val xchangeMap: ConcurrentHashMap<String, Disposable> = ConcurrentHashMap()
    fun start(){
        runBlocking {
            for (exchange in Exchange.values()) {
                println("\n launching ${exchange.name}")
                launch {
                val xchange = StreamingExchangeFactory.INSTANCE.createExchange(exchange.getExchangeClassName())
                //Some xchanges need  ProductSubscription
                val productSubscription = ProductSubscription
                        .create()
                        .addTicker(currencyPair)
                        .build()

                xchange.connect(productSubscription).blockingAwait()
                println("Exchange ${exchange.name} connected")
                val fileName = "tickers-${exchange.name}-${splittedCurrencyPair[0]}-${splittedCurrencyPair[1]}"
                val writer = BufferedWriter(FileWriter(fileName, true))
                writersMap.put(fileName, writer)
                val xChangeStream = xchange.streamingMarketDataService.getTicker(currencyPair).subscribe { ticker ->
                    println(ticker)
                    writer.appendln(ticker.toString())
                    writer.flush()
                }
                xchangeMap.put(exchange.name, xChangeStream)
                }
            }
        }
    }
    fun stop(){
        for(stream in xchangeMap){
            stream.value.dispose()
        }
        xchangeMap.clear()

        for (writer in writersMap){
            writer.value.close()
        }
        writersMap.clear()
        println("Resources successfully released")
    }
}

