package ai.scynet.core

import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.koin.core.context.startKoin
import org.koin.dsl.module
import processors.ContinuousStream
import processors.ContinuousStreamService
import processors.IContinuousStreamService
import java.util.*

fun main(){
    startKoin {
        printLogger()
        modules(module {
            single<Ignite> { Ignition.start() }
        })
    }

//
//    val stream = ContinuousStream<String>(UUID.randomUUID(), ignite)
//    stream.fillMissingStreamData(1L, 2L)
//    val stream2 = ContinuousStream<String>(UUID.randomUUID(), ignite)
//    stream2.fillMissingStreamData(1L, 2L)
}