package ai.scynet.protocol

import ai.scynet.common.registry.IgniteJobRegistry
import ai.scynet.common.registry.IgniteRegistry
import ai.scynet.common.registry.Registry
import common.registry.JobRegistry
import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.apache.ignite.Ignition.ignite
import org.apache.ignite.configuration.IgniteConfiguration
import org.koin.core.context.startKoin
import org.koin.dsl.module

/**

 * The first function to be executed
 * @param args The arguments we are receiving
 */
fun main(args: Array<String>) {
    val cfg = IgniteConfiguration()
    cfg.setIgniteInstanceName("ProtocolTest")
    val ignite = Ignition.start(cfg)
    startKoin {

                printLogger()
        modules(module {
            single<Ignite> { ignite }
            single<JobRegistry<String>> {IgniteJobRegistry<String>()}
            single<Registry<String,String>> {IgniteRegistry<String, String>("datasetRegistry")}
        })
    }
    val services = ignite("ProtocolTest").services()
    services.deployClusterSingleton("Protocol", IgniteProtocol<String>())
}

