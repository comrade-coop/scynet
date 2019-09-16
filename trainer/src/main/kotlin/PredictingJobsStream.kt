package ai.scynet.queen
import ai.scynet.protocol.TrainingJob
import ai.scynet.trainer.PredicterService
import processors.ILazyStreamService
import processors.LazyStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.KClass

class PredictingJobsStream: LazyStream<Long, TrainingJob> { // TODO: These definitions should be in the script file
    override val classId: String = "PredictingLazyStream"
    override val streamServiceClass: KClass<out ILazyStreamService> = PredicterService::class
    constructor(): super()
    constructor(id: UUID, inputStreamIds: ArrayList<UUID>?, properties: descriptors.Properties) : super(id, inputStreamIds, properties)
}