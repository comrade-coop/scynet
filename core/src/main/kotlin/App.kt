import org.apache.ignite.Ignition

fun main(args: Array<String>) {
    var ignite = Ignition.start()

    println(ignite)
}