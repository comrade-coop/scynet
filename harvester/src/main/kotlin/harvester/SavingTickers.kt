package harvester

import harvester.exchanges.TickerSaver
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.knowm.xchange.currency.CurrencyPair
import kotlin.system.exitProcess

fun main(){
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
