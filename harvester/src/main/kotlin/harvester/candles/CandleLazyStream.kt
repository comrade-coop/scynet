package harvester.candles

import descriptors.Properties
import processors.ILazyStreamService
import processors.LazyStream
import java.util.*
import kotlin.reflect.KClass

class CandleLazyStream: LazyStream<CandleDTO>{
    override val classId: String = "candleStream"
    override val streamServiceClass: KClass<out ILazyStreamService> = CandleStreamService::class
    constructor(): super()
    constructor(id: UUID, inputStreamId: UUID, properties: Properties) : super(id, inputStreamId, properties)
}