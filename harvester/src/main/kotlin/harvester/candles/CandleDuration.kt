package harvester.candles

import org.knowm.xchange.dto.marketdata.Ticker
import java.time.Instant

enum class CandleDuration: ICandleDuration {
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
}