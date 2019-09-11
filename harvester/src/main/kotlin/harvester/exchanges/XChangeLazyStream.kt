package harvester.exchanges

import descriptors.Properties
import org.knowm.xchange.dto.marketdata.Ticker
import processors.ILazyStreamService
import processors.LazyStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.reflect.KClass

class XChangeLazyStream: LazyStream<Long, Ticker>{
    override val classId: String = "xchangeStream"
    override val streamServiceClass: KClass<out ILazyStreamService> = XChangeStreamService::class
    constructor(): super()
    constructor(id: UUID, inputStreamIds: ArrayList<UUID>?, properties: Properties) : super(id, inputStreamIds, properties)
}