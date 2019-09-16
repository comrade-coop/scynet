package ai.scynet.trainer

import ai.scynet.protocol.TrainingJob
import descriptors.Properties
import processors.ILazyStreamService
import processors.LazyStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.KClass

class TrainingJobsStream : LazyStream<Long, TrainingJob> {
    override val streamServiceClass: KClass<out ILazyStreamService> = TrainerService::class
    override val classId: String = "trainingJobsStream"

    constructor(): super()
    constructor(id: UUID, inputStreamIds: ArrayList<UUID>?, properties: Properties): super(id, inputStreamIds, properties)
}