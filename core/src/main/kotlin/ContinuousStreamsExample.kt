package ai.scynet.core

import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.koin.core.context.startKoin
import org.koin.dsl.module

fun main(){
    startKoin {
        printLogger()
        modules(module {
            single<Ignite> { Ignition.start() }
        })
    }

//
//    val stream = LazyStream<String>(UUID.randomUUID(), ignite)
//    stream.fillMissingStreamData(1L, 2L)
//    val stream2 = LazyStream<String>(UUID.randomUUID(), ignite)
//    stream2.fillMissingStreamData(1L, 2L)
}