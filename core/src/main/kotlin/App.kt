package ai.scynet.core

import org.apache.ignite.Ignition

/**

 * The first function to be executed
 * @param args The arguments we are receiving
 */
fun main(args: Array<String>) {
    var ignite = Ignition.start()

    println(ignite)
}


/**
 * @property data The number to show
 */
class Hello(val data: Int) {

    init {
        println(data)
    }
}

