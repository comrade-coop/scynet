package harvester.exchanges

import ai.scynet.core.processors.IgniteStream
import ai.scynet.core.processors.Processor
import info.bitrich.xchangestream.core.ProductSubscription
import info.bitrich.xchangestream.core.StreamingExchange
import info.bitrich.xchangestream.core.StreamingExchangeFactory
import io.reactivex.disposables.Disposable
import org.knowm.xchange.currency.CurrencyPair
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

class XChangeHarvester: Processor, KoinComponent {
    override lateinit var id: UUID
    override lateinit var inputStreams: MutableList<Stream>
    override lateinit var outputStream: Stream
    override lateinit var descriptor: ProcessorDescriptor
    private lateinit var xchange: StreamingExchange
    private lateinit var xChangeStream: Disposable
    private lateinit var currencyPair: CurrencyPair
    private lateinit var outputStreamName: String
    private var genesis: Boolean = false
    private val registry: Registry<String,Stream> by inject()

    override fun start() {
        val exchange = descriptor.properties.get("xchange") as IExchange
        currencyPair = descriptor.properties.get("currencyPair") as CurrencyPair
        xchange = StreamingExchangeFactory.INSTANCE.createExchange(exchange.getExchangeClassName())
        outputStreamName = "${exchange}-${currencyPair}-TickerHarvester"
        println("Starting $outputStreamName")
        //Some xchanges need  ProductSubscription
        val productSubscription = ProductSubscription
                .create()
                .addTicker(currencyPair)
                .build()

        xchange.connect(productSubscription).blockingAwait()
        println("Exchange connected")
        xChangeStream = xchange.streamingMarketDataService.getTicker(currencyPair).subscribe {
            ticker -> println(ticker)
            if (!genesis){
                initOutputStream(ticker.timestamp.time)
                genesis = true
            }
            outputStream.append(ticker.timestamp.time, ticker)
        }
    }
    
    //Cannot initialize output stream in init block because descriptor is not yet instantiated
    //Output stream name should look like: Exchange-CurrencyPair-TickerHarvester e.g. COINBASE_PRO-BTC/USD-TickerHarvester
    private fun initOutputStream(genesisTimestamp: Long){
        val streamInRegistry = registry.get(outputStreamName)
        if(streamInRegistry == null){
            outputStream = IgniteStream(outputStreamName, id.toString(), "crypto trading", Properties().apply { put("genesis", genesisTimestamp.toString()) })
            registry.put(outputStreamName, outputStream)
        } else{
            outputStream = streamInRegistry
        }

    }
    
    override fun stop() {
        println("Stopping $outputStreamName")
        xChangeStream.dispose()
        xchange.disconnect()
    }
}
