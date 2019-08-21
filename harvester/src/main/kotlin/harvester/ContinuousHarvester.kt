package harvester

import harvester.candles.CandleDTO
import harvester.candles.CandleLazyStream
import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.apache.ignite.configuration.IgniteConfiguration
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.util.*
import kotlin.system.exitProcess

fun main(){
    val cfg = IgniteConfiguration()
    cfg.igniteInstanceName = "HarvesterTest"
    val ignite = Ignition.start(cfg)

    startKoin {
        printLogger()
        modules(module {
            single<Ignite> { ignite }
        })
    }

    val lazyCandleStream = CandleLazyStream(UUID.randomUUID())

    var cursor = lazyCandleStream.listen{ k: Long, v: CandleDTO, _ ->
        println("\nStream Output for $k  -->  $v\n")
    }
    Thread.sleep(180000)
    cursor.close()

    println("\nDisengaging Candle Stream \n")
    lazyCandleStream.disengageStream()

    Thread.sleep(30000)

    println("\nRestarting stream\n")

    cursor = lazyCandleStream.listen{ k: Long, v: CandleDTO, _ ->
        println("\nStream Output for $k  -->  $v\n")
    }
    Thread.sleep(180000)
    cursor.close()

    println("\nDisengaging Candle Stream \n")
    lazyCandleStream.disengageStream()
    Thread.sleep(30000)

    exitProcess(0)
}