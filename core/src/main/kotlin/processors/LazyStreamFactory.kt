package processors

import descriptors.LazyStreamDescriptor
import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.services.ServiceContext
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.collections.HashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

class LazyStreamFactory: ILazyStreamFactory, KoinComponent {
    
    private val ignite: Ignite by inject()
    private val streamClasses: HashMap<String, KClass<out ILazyStream>>
    private lateinit var streamClassIds: IgniteCache<UUID, String>
    private lateinit var streamDescriptors: IgniteCache<UUID, LazyStreamDescriptor>

    constructor(){
        streamClasses =  HashMap<String, KClass<out ILazyStream>>()
    }
    constructor(streamClasses:  HashMap<String, KClass<out ILazyStream>>){
        this.streamClasses = streamClasses
    }

    override fun init(ctx: ServiceContext?) {
        streamDescriptors = ignite.getOrCreateCache("streamDescriptors")
        streamClassIds = ignite.getOrCreateCache("streamClassIds")
    }

    override fun cancel(ctx: ServiceContext?) {
    }

    override fun execute(ctx: ServiceContext?) {
    }

    override fun registerStream(stream: ILazyStream){
        val streamId = stream.descriptor!!.id
        if(streamClassIds.containsKey(streamId)){
            throw IllegalArgumentException("Stream with id: ${streamId} already registerd!")
        }
        streamClassIds.put(streamId, stream.classId)
        streamDescriptors.put(streamId, stream.descriptor)
        registerStreamClass(stream)
    }

    override fun getInstance(streamId: UUID): ILazyStream{
        if(!streamDescriptors.containsKey(streamId))
            throw IllegalArgumentException("Stream with id: $streamId does not exist!")
        val streamClassId = streamClassIds.get(streamId)
        val stream = streamClasses[streamClassId]!!.createInstance()
        val streamDescriptor = streamDescriptors.get(streamId)
        stream.descriptor = streamDescriptor
        return stream
    }

    private fun registerStreamClass(stream: ILazyStream){
        if(!streamClasses.containsKey(stream.classId))
            streamClasses.put(stream.classId, stream::class)
    }
}