package harvester.indicators

import processors.ILazyStreamService
import processors.LazyStream
import java.util.*
import kotlin.reflect.KClass
import descriptors.Properties

class SimpleMovingAverageStream: LazyStream<Long, Double> {
    override val classId: String = "simpleMovingAverageStream"
    override val streamServiceClass: KClass<out ILazyStreamService> = SimpleMovingAverageService::class
    constructor(): super()
    constructor(id: UUID, inputStreamId: UUID, properties: Properties): super(id, arrayListOf(inputStreamId), properties)
}