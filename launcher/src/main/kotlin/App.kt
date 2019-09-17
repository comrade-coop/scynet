package ai.scynet.launcher

import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.apache.ignite.configuration.IgniteConfiguration
import org.koin.core.context.startKoin
import org.koin.dsl.module

fun main(args: Array<String>) {
	val cfg = IgniteConfiguration()
	cfg.igniteInstanceName = "Scynet"
	//cfg.setPeerClassLoadingEnabled(true)
	val ignite = Ignition.start(cfg)

	startKoin {
		printLogger()
		modules(module {
			single<Ignite> { ignite }
		})
	}

	val service = LauncherService()
	ignite.services().deployClusterSingleton("launcher", service)
}
