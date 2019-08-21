package harvester.candles

import org.apache.ignite.services.ServiceContext
import org.knowm.xchange.dto.marketdata.Ticker
import processors.LazyStream
import processors.LazyStreamService
import java.util.*
import kotlin.collections.HashMap

class CandleStreamService(properties: Properties, private val inputStream: LazyStream<Ticker>): LazyStreamService<Candle>(){

    private var timer = Timer()

    private  val candle = properties["candle"] as ICandle
    private lateinit var tickerStream: AutoCloseable
    private val buffer: HashMap<Long, Ticker> = HashMap()
    private var genensis: Boolean = true
    private val genesisBuffer: HashMap<Long, Ticker> = HashMap()

    private fun fillGenesisBuffer(timestamp: Long, ticker: Ticker){
        genesisBuffer[timestamp] = ticker
        if(genesisBuffer.size > 10){
            candle.setInitialCandleTimestamp(choseFirstTimestamp())
            genesisBuffer.forEach{ (timestamp, ticker) -> fillCandle(timestamp, ticker)}
            genensis = false
            genesisBuffer.clear()
        }
    }
    private fun choseFirstTimestamp(): Long{
        var firstTimestamp = Long.MAX_VALUE
        genesisBuffer.forEach { (timestamp, ticker) ->
            if(timestamp < firstTimestamp)
                firstTimestamp = timestamp
        }
        return firstTimestamp
    }

    private fun fillCandle(timestamp: Long, ticker: Ticker){
        println("Ticker as received from XChangeStream $ticker")
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
    override fun execute(ctx: ServiceContext?) {
        super.execute(ctx)

        tickerStream = inputStream.listen{
            timestamp: Long, ticker: Ticker, _ ->
            if(genensis){
                fillGenesisBuffer(timestamp, ticker)
            }else{
                fillCandle(timestamp, ticker)
            }
        }
    }
    private fun streamCandle(){
        val candle = candle.getCandle()
        println("\n $candle \n")
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

    override fun cancel(ctx: ServiceContext?) {
        tickerStream.close()
        inputStream.disengageStream()
        super.cancel(ctx)
    }
}