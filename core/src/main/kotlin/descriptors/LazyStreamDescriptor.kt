package descriptors

import java.util.*

data class LazyStreamDescriptor(
    var id: UUID,
//    var streamClassId: String,
    var serviceDescriptor: LazyStreamServiceDescriptor
)