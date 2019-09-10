package ai.scynet.executor

import ai.scynet.executor.QueryService
import ai.scynet.protocol.TrainingJob
import descriptors.Properties
import processors.ILazyStreamService
import processors.LazyStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.KClass

class QueryStream : LazyStream<TrainingJob>{
    override val streamServiceClass: KClass<out ILazyStreamService> = QueryService::class
    override val classId: String = "queryStream"

    constructor(): super()
    constructor(id: UUID, inputStreamIds: ArrayList<UUID>?, properties: Properties): super(id,inputStreamIds, properties)
}