package ai.scynet.executor

import ai.scynet.executor.ExecutorService
import ai.scynet.protocol.TrainingJob
import descriptors.Properties
import processors.ILazyStreamService
import processors.LazyStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.KClass

class OutputStream : LazyStream<Any>{
    override val streamServiceClass: KClass<out ILazyStreamService> = ExecutorService::class
    override val classId: String = "outputStream"

    constructor(): super()
    constructor(id: UUID, inputStreamIds: ArrayList<UUID>?, properties: Properties): super(id, inputStreamIds, properties)
}