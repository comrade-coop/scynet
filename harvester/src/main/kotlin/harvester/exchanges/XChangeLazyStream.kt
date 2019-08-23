package harvester.exchanges

import org.knowm.xchange.dto.marketdata.Ticker
import processors.LazyStream

class XChangeLazyStream: LazyStream<Ticker>()