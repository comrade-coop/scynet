package harvester.labels

import processors.ILazyStreamService
import processors.LazyStream
import java.util.*
import kotlin.reflect.KClass
import descriptors.Properties

class CandleLabelStream: LazyStream<Long, Boolean> {
    constructor(): super()
    constructor(id: UUID, inputStreamId: UUID, properties: Properties): super(id, arrayListOf(inputStreamId), properties)
    override val streamServiceClass: KClass<out ILazyStreamService> = CandleLabelService::class
    override val classId: String = "candleLabelStream"
}