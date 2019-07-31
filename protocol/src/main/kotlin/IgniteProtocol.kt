package ai.scynet.protocol

import ai.scynet.common.registry.JobRegistry
import ai.scynet.common.registry.Registry
import org.apache.ignite.services.Service
import org.apache.ignite.services.ServiceContext
import org.koin.core.KoinComponent

class IgniteProtocol<K>: Service, Protocol, KoinComponent {
    override fun init(ctx: ServiceContext?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun cancel(ctx: ServiceContext?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun execute(ctx: ServiceContext?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override  val jobRegistry: JobRegistry<K> = JobRegistry()
    override val datasetRegistry: Registry<String, Dataset<*, *>>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
}