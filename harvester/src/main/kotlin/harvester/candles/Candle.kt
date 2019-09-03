package harvester.candles

import org.knowm.xchange.dto.marketdata.Ticker
import java.time.Instant

enum class Candle: ICandle {
    MINUTE{
        override fun getDuration(): Long = 60000
    },
    QUARTER{
        override fun getDuration(): Long = 900000

    },
    HALF{
        override fun getDuration(): Long = 1800000
    },
    HOUR{
        override fun getDuration(): Long = 3600000
    };

    override lateinit var beginningOfTick: Instant
    override lateinit var endOfTick: Instant
    private val tickers: MutableList<Ticker> = mutableListOf()
    private var openingTickerTimestamp: Long = Long.MAX_VALUE
    private var openTickerIndex: Int = -1
    private var high: Double = 0.0
    private var low: Double = Double.MAX_VALUE
    private var open: Double? = null
    private var bidAskAvg: Double = 0.0

    override fun setInitialCandleTimestamp(genesisTickerTimestamp: Long){
        val instance = Instant.ofEpochMilli(genesisTickerTimestamp)
        val timeToSubtract = instance.toEpochMilli() % getDuration()
        beginningOfTick = instance.minusMillis(timeToSubtract)
        endOfTick = beginningOfTick.plusMillis(getDuration())

    }

    override fun addTicker(ticker: Ticker) {
        val average = (ticker.bid.toDouble() + ticker.ask.toDouble()) / 2
        openCandidate(ticker.timestamp.time)
        if(average < low)
            low = average
        if(average > high)
            high = average
        bidAskAvg += average
        tickers.add(ticker)
    }

    override fun getCandle(): CandleDTO {
        bidAskAvg /= tickers.size
        val candle = CandleDTO(calculateOpen(), bidAskAvg, high, low, beginningOfTick.toEpochMilli())
        reset()
        return candle
    }

    private fun reset(){
        open = bidAskAvg
        bidAskAvg = 0.0
        high = 0.0
        low = Double.MAX_VALUE
        tickers.clear()
        nextTick()

    }

    private fun nextTick(){
        beginningOfTick = endOfTick
        endOfTick = beginningOfTick.plusMillis(getDuration())
    }

    private fun calculateOpen(): Double{
        if(open == null)
            open = if (tickers.size == 0)  null else (tickers.get(openTickerIndex).ask.toDouble() + tickers.get(openTickerIndex).bid.toDouble()) / 2
        return open as Double
    }

    private fun openCandidate(timestamp: Long){
        if(timestamp < openingTickerTimestamp){
            openingTickerTimestamp = timestamp
            openTickerIndex = tickers.size
        }
    }
}