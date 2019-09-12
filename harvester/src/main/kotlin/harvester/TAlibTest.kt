package harvester

import com.tictactec.ta.lib.Core
import com.tictactec.ta.lib.MInteger
import com.tictactec.ta.lib.RetCode
import harvester.candles.CandleDTO
import org.apache.ignite.services.ServiceContext
import processors.LazyStreamService
import java.util.*

object SimpleMovingAverageExample {

    /**
     * The total number of periods to generate data for.
     */
    val TOTAL_PERIODS = 30

    /**
     * The number of periods to average together.
     */
    val PERIODS_AVERAGE = 30

    @JvmStatic
    fun main(args: Array<String>) {
        val closePrice = DoubleArray(TOTAL_PERIODS)
        val out = DoubleArray(TOTAL_PERIODS)
        val begin = MInteger()
        val length = MInteger()

        for (i in closePrice.indices) {
            closePrice[i] = i.toDouble()
        }

        val c = Core()
        val retCode = c.sma(0, closePrice.size - 1, closePrice, PERIODS_AVERAGE, begin, length, out)


        if (retCode == RetCode.Success) {
            println("Output Start Period: " + begin.value)
            println("Output End Period: " + (begin.value + length.value - 1))

            for (i in begin.value until begin.value + length.value) {
                val line = StringBuilder()
                line.append("Period #")
                line.append(i)
                line.append(" close=")
                line.append(closePrice[i])
                line.append(" mov_avg=")
                line.append(out[i - begin.value])
                println(line.toString())
            }
        } else {
            println("Error")
        }
    }
}

