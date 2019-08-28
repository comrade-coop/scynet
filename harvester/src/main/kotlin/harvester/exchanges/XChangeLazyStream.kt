package harvester.exchanges

import descriptors.Properties
import org.knowm.xchange.dto.marketdata.Ticker
import processors.ILazyStreamService
import processors.LazyStream
import java.util.*
import kotlin.collections.HashMap
import kotlin.reflect.KClass

class XChangeLazyStream: LazyStream<Ticker>{
    override val classId: String = "xchangeStream"
    override val streamServiceClass: KClass<out ILazyStreamService> = XChangeStreamService::class
    constructor(): super()
    constructor(id: UUID, inputStreamId: UUID, properties: Properties) : super(id, inputStreamId, properties)
}