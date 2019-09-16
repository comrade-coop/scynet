package ai.scynet.trainer

import ai.scynet.protocol.TrainingJob
import descriptors.Properties
import processors.ILazyStreamService
import processors.LazyStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.KClass

class SelectedJobsStream : LazyStream<Long, TrainingJob>{
    override val streamServiceClass: KClass<out ILazyStreamService> = JobSelectionService::class
    override val classId: String = "selectedJobsStream"

    constructor(): super()
    constructor(id: UUID, inputStreamIds: ArrayList<UUID>?, properties: Properties): super(id, inputStreamIds, properties)
}