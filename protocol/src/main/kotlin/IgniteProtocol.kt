package ai.scynet.protocol

import ai.scynet.common.registry.Registry
import common.registry.JobRegistry
import org.apache.ignite.Ignite
import org.apache.ignite.services.Service
import org.apache.ignite.services.ServiceContext
import org.koin.core.KoinComponent
import org.koin.core.inject

class IgniteProtocol<K>: Protocol<K>(), Service, KoinComponent {

    override val datasetRegistry: Registry<K, Dataset<*, *>> by inject()
    override val jobRegistry: JobRegistry<K> by inject()
    private val ignite: Ignite by inject()


    override fun init(ctx: ServiceContext?) {
        println("Initialized ${ctx!!.name()}")
    }

    override fun cancel(ctx: ServiceContext?) {
        println("Cancelling ${ctx!!.name()}")
    }

    override fun execute(ctx: ServiceContext?) {
        println("Executing ${ctx!!.name()}")
    }
}