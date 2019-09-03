package harvester.candles

import ai.scynet.common.registry.Registry
import ai.scynet.core.descriptors.ProcessorDescriptor
import ai.scynet.core.processors.IgniteStream
import ai.scynet.core.processors.Processor
import ai.scynet.core.processors.Stream
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.knowm.xchange.dto.marketdata.Ticker
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*
import kotlin.collections.HashMap

class CandleProcessor: Processor, KoinComponent {

    override lateinit var id: UUID
    override lateinit var inputStreams: MutableList<Stream>
    override lateinit var outputStream: Stream
    override lateinit var descriptor: ProcessorDescriptor
    private lateinit var candle: ICandle
    private lateinit var tickerStream: AutoCloseable
    private lateinit var  cache: IgniteCache<String, String>
    private lateinit var NAME: String
    private val ignite: Ignite by inject()
    private val registry: Registry<String, Stream> by inject()
    private val LAST_CANDLE_TIMESTAMP: String = "lastCandleTimestamp"
    private val buffer: HashMap<Long, Ticker> = HashMap()

    override fun start() {
        initCandle()
        NAME = "$candle-Candle-Processor"
        println("Starting $NAME")
        initCache()
        initOutputStream()
        candle.setInitialCandleTimestamp(getInitialTimestamp())

        tickerStream = inputStreams[0].listen{
            timestamp: Long, ticker: Ticker, _ ->
            val candleTimestamp = candle.endOfTick.toEpochMilli()

            if(buffer.size > 100){
                streamCandle()
                emptyBuffer()
            }
            if(timestamp > candleTimestamp){
                buffer.put(timestamp, ticker)
            }else if(timestamp < candle.beginningOfTick.toEpochMilli()){
                //We've already formed the candle, so we skip this ticker. Find a way to add ticker to candle later.
                println("Skipping $ticker")
            }else{
                candle.addTicker(ticker)
            }
        }
    }

    override fun stop() {
        tickerStream.close()
        println("Stopping $NAME")
        cache.close()
    }

    private fun initCache(){
        cache = ignite.getOrCreateCache(NAME)
    }

    private fun getInitialTimestamp(): Long{
        if(cache.containsKey(LAST_CANDLE_TIMESTAMP))
            return cache.get(LAST_CANDLE_TIMESTAMP).toLong()
        return (inputStreams[0].descriptor.properties.get("genesis") as String).toLong()
    }

    private fun streamCandle(){
        val candle = candle.getCandle()
        println("\n $candle \n")
        outputStream.append(candle.timestamp, candle)
        cache.put(LAST_CANDLE_TIMESTAMP, this.candle.beginningOfTick.plusMillis(1).toEpochMilli().toString())
    }
    private fun emptyBuffer(){
        val toDelete = mutableListOf<Long>()
        for ((timestamp, ticker) in buffer){
            if(ticker.timestamp.toInstant().isBefore(candle.endOfTick)){
                candle.addTicker(ticker)
                toDelete.add(timestamp)
            }
        }
        for (timestamp in toDelete){
            buffer.remove(timestamp)
        }
    }

    private fun initCandle(){
        candle = descriptor.properties.get("candle") as ICandle
    }

    private fun initOutputStream(){
        val streamInRegistry = registry.get(NAME)
        if(streamInRegistry == null) {
            outputStream = IgniteStream(NAME, id.toString(), "crypto trading", Properties())
            registry.put(NAME, outputStream)
        } else{
            outputStream = streamInRegistry
        }
    }
}