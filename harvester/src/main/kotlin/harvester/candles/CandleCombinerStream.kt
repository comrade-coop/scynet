package harvester.candles

import descriptors.Properties
import org.nd4j.linalg.api.ndarray.INDArray
import processors.ILazyStreamService
import processors.LazyStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.KClass

class CandleCombinerStream: LazyStream<Long, INDArray> {
    override val streamServiceClass: KClass<out ILazyStreamService> = CandleCombiner::class

    override val classId: String = "candleCombiner"

    constructor(): super()
    constructor(id: UUID, inputStreamIds: ArrayList<UUID>?): super(id, inputStreamIds, null)

}