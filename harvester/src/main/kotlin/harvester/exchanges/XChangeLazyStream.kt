package harvester.exchanges

import org.knowm.xchange.dto.marketdata.Ticker
import processors.LazyStream
import java.util.*

class XChangeLazyStream(id: UUID, properties: Properties): LazyStream<Ticker>(id, XChangeStreamService(properties))