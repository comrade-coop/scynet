package harvester.normalization

import org.nd4j.linalg.api.ndarray.INDArray
import processors.LazyStream
import java.util.*
import processors.ILazyStreamService
import kotlin.reflect.KClass

class NormalizingStream: LazyStream<Long, INDArray> {
    constructor(): super()
    constructor(id: UUID, inputStreamId: UUID): super(id, arrayListOf(inputStreamId), null)
    override val streamServiceClass: KClass<out ILazyStreamService> = NormalizingService::class
    override val classId: String = "normalizingStream"
}