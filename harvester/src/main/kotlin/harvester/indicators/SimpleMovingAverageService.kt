package harvester.indicators

import com.tictactec.ta.lib.Core
import com.tictactec.ta.lib.MInteger
import harvester.candles.CandleDTO
import org.apache.ignite.services.ServiceContext
import processors.LazyStreamService
import java.util.*


class SimpleMovingAverageService: LazyStreamService<Long, Double>(){

    private val TALibCore = Core()
    private val closePrices: LinkedList<Double> = LinkedList()
    private var PERIODS_AVERAGE: Int? = null

    override fun init(ctx: ServiceContext?) {
        super.init(ctx)
        PERIODS_AVERAGE = descriptor!!.properties!!.get("averagingPeriod") as Int

    }

    override fun execute(ctx: ServiceContext?) {
        super.execute(ctx)
        inputStreams[0].listen{ timestamp: Long, candle: CandleDTO, _ ->
            closePrices.addLast(candle.close)
            if(closePrices.size == PERIODS_AVERAGE){
                val out = getMovingAverage()
                cache.put(timestamp, out)
                closePrices.removeFirst()
            }
        }
    }

    private fun getMovingAverage(): Double{
        val closePrice = closePrices.toDoubleArray()
        val out = DoubleArray(PERIODS_AVERAGE!!)
        val begin = MInteger()
        val length = MInteger()

        TALibCore.sma(0, closePrice.size - 1, closePrice, PERIODS_AVERAGE!!, begin, length, out)

        return out[0]
    }
}