package ai.scynet.trainer.mock

import ai.scynet.protocol.TrainingJob
import descriptors.Properties
import processors.ILazyStreamService
import processors.LazyStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.KClass

class MockJobsStream : LazyStream<TrainingJob>{
    override val streamServiceClass: KClass<out ILazyStreamService> = MockService::class
    override val classId: String = "mockJobsStream"

    constructor(): super()
    constructor(id: UUID, inputStreamIds: ArrayList<UUID>?, properties: Properties): super(id, inputStreamIds, properties)
}