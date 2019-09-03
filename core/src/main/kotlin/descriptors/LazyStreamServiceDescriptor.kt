package descriptors

import java.util.*

data class LazyStreamServiceDescriptor(
    var inputStreamId: UUID,
    var properties: Properties
)
