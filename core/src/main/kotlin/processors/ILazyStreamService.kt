package processors

import descriptors.LazyStreamServiceDescriptor
import org.apache.ignite.services.Service
import java.util.*

interface ILazyStreamService: Service{
    var descriptor: LazyStreamServiceDescriptor?

    fun engageLiveStream()

    fun fillMissingStreamData(from: Long, to: Long)

    fun fillMissingStreamData(from: Long)

    fun refreshStreamData(from: Long, to: Long)

    fun refreshStreamData(from: Long)
}