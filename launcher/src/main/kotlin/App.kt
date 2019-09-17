package ai.scynet.launcher

import harvester.exchanges.TickerSaver
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.apache.ignite.configuration.DataStorageConfiguration
import org.apache.ignite.configuration.IgniteConfiguration
import org.knowm.xchange.currency.CurrencyPair
import org.koin.core.context.startKoin
import org.koin.dsl.module
import kotlin.system.exitProcess

fun main(args: Array<String>) {
	val cfg = IgniteConfiguration()
	val storageCfg = DataStorageConfiguration()
	storageCfg.defaultDataRegionConfiguration.isPersistenceEnabled = true
	cfg.dataStorageConfiguration = storageCfg
	cfg.igniteInstanceName = "Scynet"
	//cfg.setPeerClassLoadingEnabled(true)
	val ignite = Ignition.start(cfg)

	startKoin {
		printLogger()
		modules(module {
			single<Ignite> { ignite }
		})
	}

	ignite.active(true)
	val service = LauncherService()
	ignite.services().deployClusterSingleton("launcher", service)

	GlobalScope.launch {
		val tickerWriter = TickerSaver(CurrencyPair.ETH_USD)

		Runtime.getRuntime().addShutdownHook(object : Thread() {
			override fun run() {
				tickerWriter.stop()
			}
		})
		GlobalScope.launch {
			while(readLine() != "stop"){
				println("Only stop command accepted!")
			}
			exitProcess(0)
		}

		tickerWriter.start()
	}
}
