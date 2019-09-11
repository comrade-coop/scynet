package harvester.windows

import org.nd4j.linalg.api.ndarray.INDArray
import processors.LazyStream
import java.util.*
import descriptors.Properties
import processors.ILazyStreamService
import kotlin.reflect.KClass

class WindowingStream: LazyStream<Long, INDArray> {
    override val streamServiceClass: KClass<out ILazyStreamService> = WindowingService::class
    override val classId: String = "windowingStream"
    constructor(): super()
    constructor(id: UUID, inputStreamId: UUID, properties: Properties): super(id, arrayListOf(inputStreamId), properties)

}