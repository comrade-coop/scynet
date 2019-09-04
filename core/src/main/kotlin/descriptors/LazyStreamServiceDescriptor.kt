package descriptors

import java.util.*

data class LazyStreamServiceDescriptor(
    var inputStreamIds: List<UUID>?,
    var properties: Properties?
)
