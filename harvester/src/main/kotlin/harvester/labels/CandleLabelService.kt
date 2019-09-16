package harvester.labels

import harvester.candles.CandleDTO
import org.apache.ignite.services.ServiceContext
import processors.LazyStreamService
import java.util.*

class CandleLabelService: LazyStreamService <Long, Boolean>() {
    private var upperTresholdPercentage: Double? = null
    private var lowerTresholdPercentage: Double? = null
    private var periodInMilliseconds: Long? = null
    private val gains: LinkedList<Pair<Long,Pair<Double, Double>>> = LinkedList()

    override fun init(ctx: ServiceContext?) {
        super.init(ctx)
        upperTresholdPercentage = descriptor!!.properties!!.get("upperTresholdPercentage") as Double
        lowerTresholdPercentage = descriptor!!.properties!!.get("lowerTresholdPercentage") as Double
        periodInMilliseconds = descriptor!!.properties!!.get("periodInMinutes") as Int * 60000L
    }
    override fun execute(ctx: ServiceContext?) {
        super.execute(ctx)
        inputStreams[0].listen{ _: Long, candle: CandleDTO, _ ->
            while(gains.size > 0){
                val timestamp = gains.first!!.first
                if (timestamp + periodInMilliseconds!! >= candle.timestamp){
                    break
                }
                val close = gains.first.second.first
                val upperTreshold = close * upperTresholdPercentage!! / 100
                val lowerTreshold = close * lowerTresholdPercentage!! / -100
                gains.removeFirst()
                val label = calculateLabel(upperTreshold, lowerTreshold)
                cache.put(timestamp, label)
            }
            val currentGain = candle.close - candle.open
            gains.addLast(Pair(candle.timestamp,Pair(candle.close, currentGain)))
        }
    }

    private fun calculateLabel(upperTreshold: Double, lowerTreshold: Double): Boolean{
        var label = false
        var gain = 0.0
        for(g in gains){
            val currentGain = g.second.second
            gain += currentGain
            if(gain > upperTreshold){
                label = true
                break
            }
            else if(gain < lowerTreshold)
                break
        }
        return  label
    }
}