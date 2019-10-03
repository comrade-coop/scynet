package harvester.candles

import org.apache.ignite.services.ServiceContext
import org.knowm.xchange.dto.marketdata.Ticker
import processors.LazyStreamService
import kotlin.collections.HashMap

class CandleStreamService: LazyStreamService<Long, CandleDTO>(){
    private  lateinit var candle: ICandle
    private lateinit var tickerStream: AutoCloseable
    private val buffer: HashMap<Long, Ticker> = HashMap()
    private var genesis: Boolean = true
    private val genesisBuffer: HashMap<Long, Ticker> = HashMap()

    override fun init(ctx: ServiceContext?) {
        super.init(ctx)
        candle = Candle(descriptor!!.properties!!.get("candle") as ICandleDuration)
    }

    override fun execute(ctx: ServiceContext?) {
        super.execute(ctx)

        tickerStream = inputStreams[0].listen{
            timestamp: Long, ticker: Ticker, _ ->
            if(genesis){
                fillGenesisBuffer(timestamp, ticker)
            }else{
                fillCandle(timestamp, ticker)
            }
        }
    }

    private fun fillGenesisBuffer(timestamp: Long, ticker: Ticker){
        genesisBuffer[timestamp] = ticker
        if(genesisBuffer.size > 10){
            candle.setInitialCandleTimestamp(choseFirstTimestamp())
            genesisBuffer.forEach{ (timestamp, ticker) -> fillCandle(timestamp, ticker)}
            genesis = false
            genesisBuffer.clear()
        }
    }

    override fun cancel(ctx: ServiceContext?) {
        tickerStream.close()
        super.cancel(ctx)
    }

    private fun choseFirstTimestamp(): Long{
        var firstTimestamp = Long.MAX_VALUE
        genesisBuffer.forEach { (timestamp, _) ->
            if(timestamp < firstTimestamp)
                firstTimestamp = timestamp
        }
        return firstTimestamp
    }

    private fun fillCandle(timestamp: Long, ticker: Ticker){
        //println("Ticker as received from XChangeStream $ticker")
        val candleTimestamp = candle.endOfTick.toEpochMilli()

        if(buffer.size > 20){
            streamCandle()
            emptyBuffer()
        }
        if(timestamp > candleTimestamp){
            buffer.put(timestamp, ticker)
        }else if(timestamp < candle.beginningOfTick.toEpochMilli()){
            //We've already formed the candle, so we skip this ticker. Find a way to add ticker to candle later.
            logger.trace("Skipping $ticker")
        }else{
            candle.addTicker(ticker)
        }
    }

    private fun streamCandle(){
        val candle = candle.getCandle()
        if(candle != null){
            cache.put(candle.timestamp, candle)
        }
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
}