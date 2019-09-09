package ai.scynet.queen
import ai.scynet.protocol.TrainingJob
import processors.ILazyStreamService
import processors.LazyStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.KClass

class GattakkaLazyStream: LazyStream<TrainingJob<*, *>> { // TODO: These definitions should be in the script file
    override val classId: String = "gattakkaLazyStream"
    override val streamServiceClass: KClass<out ILazyStreamService> = GattakkaStreamService::class
    constructor(): super()
    constructor(id: UUID, inputStreamIds: ArrayList<UUID>?, properties: descriptors.Properties) : super(id, inputStreamIds, properties)
}