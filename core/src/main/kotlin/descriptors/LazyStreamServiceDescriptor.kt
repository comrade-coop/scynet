package descriptors

import processors.ILazyStreamService
import java.util.*
import kotlin.collections.HashMap
import kotlin.reflect.KClass

data class LazyStreamServiceDescriptor(
    var streamServiceClass: KClass<out ILazyStreamService>,
    var inputStreamId: UUID,
    var properties: HashMap<String, *>
)
