package harvester.indicators

import processors.ILazyStreamService
import processors.LazyStream
import java.util.*
import kotlin.reflect.KClass
import descriptors.Properties
import org.nd4j.linalg.api.ndarray.INDArray

class CompositeLengthIndicatorStream: LazyStream<Long, INDArray> {
    override val classId: String = "compositeLengthIndicatorStream"
    override val streamServiceClass: KClass<out ILazyStreamService> = CompositeLengthIndicatorService::class
    constructor(): super()
    constructor(id: UUID, inputStreamId: UUID, properties: Properties): super(id, arrayListOf(inputStreamId), properties)
}