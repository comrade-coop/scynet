package harvester.datasets

import descriptors.Properties
import org.nd4j.linalg.api.ndarray.INDArray
import processors.ILazyStreamService
import processors.LazyStream
import java.util.*
import kotlin.reflect.KClass

class DatasetStream: LazyStream<String, INDArray> {
    constructor(): super()
    constructor(id: UUID, inputStreamId: UUID, properties: Properties): super(id, arrayListOf(inputStreamId), properties)
    override val classId: String = "datasetStream"
    override val streamServiceClass: KClass<out ILazyStreamService> = DatasetService::class
}