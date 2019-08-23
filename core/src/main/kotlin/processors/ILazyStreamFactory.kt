package processors

import descriptors.LazyStreamDescriptor
import org.apache.ignite.services.Service
import java.util.*

interface ILazyStreamFactory: Service {
    fun registerStream(streamDescriptor: LazyStreamDescriptor)
    fun getInstance(streamId: UUID): ILazyStream
}