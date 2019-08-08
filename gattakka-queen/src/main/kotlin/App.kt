package ai.scynet.queen

import ai.scynet.core.configurations.ConfigurationHost
import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.koin.core.context.startKoin
import org.koin.dsl.module

fun main(args: Array<String>) {
    val host = ConfigurationHost()
    val config = host.getIgniteConfiguration("ignite.kts")

    val koin = startKoin {
        printLogger()
        modules(module {
            single<Ignite> { Ignition.start(config) }
        })
    }.koin

    val ignite = koin.get<Ignite>()
    ignite.cluster().active()

    ignite.executorService().execute {
        var queen = GattakkaQueen()
        queen.start()
    }




}
