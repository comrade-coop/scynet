package harvester.combiners

import org.nd4j.linalg.api.ndarray.INDArray
import processors.ILazyStreamService
import processors.LazyStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.KClass

class INDArrayCombinerStream: LazyStream<Long, INDArray> {
    override val classId: String = "INDArrayCombinerStream"
    override val streamServiceClass: KClass<out ILazyStreamService> = INDArrayCombinerService::class
    constructor(): super()
    constructor(id: UUID, inputStreamIds: ArrayList<UUID>): super(id, inputStreamIds, null)
}