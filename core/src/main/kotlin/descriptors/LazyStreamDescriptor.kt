package descriptors

import processors.ILazyStream
import java.util.*
import kotlin.reflect.KClass

data class LazyStreamDescriptor(
    var id: UUID,
    var streamClass: KClass<out ILazyStream>,
    var serviceDescriptor: LazyStreamServiceDescriptor
)