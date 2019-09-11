package harvester.candles

import org.knowm.xchange.dto.marketdata.Ticker
import java.time.Instant

interface ICandle {
    fun setInitialCandleTimestamp(genesisTickerTimestamp: Long)
    fun getDuration(): Long
    fun addTicker(ticker: Ticker)
    fun getCandle(): CandleDTO?
    var endOfTick: Instant
    var beginningOfTick: Instant
}