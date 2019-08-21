package harvester.candles

import org.knowm.xchange.dto.marketdata.Ticker
import processors.LazyStream
import java.util.*

class CandleLazyStream(inputStream: LazyStream<Ticker>, properties: Properties): LazyStream<Candle>(UUID.randomUUID(), CandleStreamService(properties, inputStream)) {
}