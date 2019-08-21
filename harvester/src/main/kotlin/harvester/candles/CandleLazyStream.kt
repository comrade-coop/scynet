package harvester.candles

import processors.ILazyStreamService
import processors.LazyStream
import java.util.*

val candleServiceProperties = Properties().apply {
    put("candle", Candle.MINUTE)
}

class CandleLazyStream(id: UUID): LazyStream<CandleDTO>(id) {
    override val streamService: ILazyStreamService<CandleDTO> = CandleStreamService(candleServiceProperties)
}