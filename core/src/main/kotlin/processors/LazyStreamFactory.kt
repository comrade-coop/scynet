package processors

import descriptors.LazyStreamDescriptor
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.services.ServiceContext
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.reflect.full.createInstance

class LazyStreamFactory: ILazyStreamFactory, KoinComponent {
    private val ignite: Ignite by inject()
    private lateinit var streamDescriptors: IgniteCache<UUID, LazyStreamDescriptor>

    override fun init(ctx: ServiceContext?) {
        streamDescriptors = ignite.getOrCreateCache("streamDescriptors")
    }

    override fun cancel(ctx: ServiceContext?) {
    }

    override fun execute(ctx: ServiceContext?) {
    }

    override fun registerStream(streamDescriptor: LazyStreamDescriptor){
        if(streamDescriptors.containsKey(streamDescriptor.id)){
            throw IllegalArgumentException("Stream with id: ${streamDescriptor.id} already registerd!")
        }
        streamDescriptors.put(streamDescriptor.id, streamDescriptor)
    }

    override fun getInstance(streamId: UUID): ILazyStream{
        if(!streamDescriptors.containsKey(streamId))
            throw IllegalArgumentException("Stream with id: $streamId does not exist!")
    val streamDescriptor = streamDescriptors.get(streamId)
    val stream = streamDescriptor.streamClass.createInstance()
    stream.descriptor = streamDescriptor
    return stream
    }
}