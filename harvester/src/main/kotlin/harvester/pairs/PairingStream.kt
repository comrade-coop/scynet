package harvester.pairs

import org.nd4j.linalg.api.ndarray.INDArray
import processors.LazyStream
import java.util.*
import kotlin.collections.ArrayList
import descriptors.Properties
import processors.ILazyStreamService
import kotlin.reflect.KClass

class PairingStream: LazyStream<Long, Pair<Boolean, INDArray>> {
    constructor(): super()
    constructor(id: UUID, inputStreamIds: ArrayList<UUID>): super(id, inputStreamIds, null)
    override val classId: String = "pairingStream"
    override val streamServiceClass: KClass<out ILazyStreamService> = PairingService::class
}