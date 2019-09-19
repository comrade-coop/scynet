package harvester.indicators

import com.tictactec.ta.lib.Core
import com.tictactec.ta.lib.MInteger
import harvester.candles.CandleDTO
import org.apache.ignite.services.ServiceContext
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import processors.LazyStreamService
import java.util.*
import kotlin.collections.ArrayList


class CompositeLengthIndicatorService: LazyStreamService<Long, INDArray>(){

    private val TALibCore = Core()
    private var maxLenght: Int = Int.MIN_VALUE
    private lateinit var indicators: ArrayList<Pair<String, Int>>
    override fun init(ctx: ServiceContext?) {
        super.init(ctx)
        indicators = descriptor!!.properties!!.get("indicators") as ArrayList<Pair<String, Int>>
        for(indicator in indicators){
            val length = indicator.second
            if(length > maxLenght)
                maxLenght = length
        }
    }

    override fun execute(ctx: ServiceContext?) {
        super.execute(ctx)

        val candles: LinkedList<CandleDTO> = LinkedList()
        inputStreams[0].listen{ timestamp: Long, candle: CandleDTO, _ ->
            candles.addLast(candle)
            if(candles.size == maxLenght){
                val out = getIndicatorsValue(toCandleINDArray(candles))
                cache.put(timestamp, out)
                candles.removeFirst()
            }
        }
    }

    private fun getIndicatorsValue(candles: Array<CandleDTO?>): INDArray {
        val combinedIndicators = DoubleArray(indicators.size)
        val pricesInGivenLength = HashMap<String, DoubleArray>()
        for(indicator in indicators){
            val length = indicator.second
            val currentCandles = candles.copyOfRange(candles.size - length , candles.size)
            if(pricesInGivenLength.containsKey("closePrices$length")){
                break
            }
            val closePrices =  currentCandles.map { it!!.close }.toDoubleArray()
            pricesInGivenLength["closePrices$length"] = closePrices

            val highPrices = currentCandles.map { it!!.high }.toDoubleArray()
            pricesInGivenLength["highPrices$length"] = highPrices

            val lowPrices = currentCandles.map { it!!.low }.toDoubleArray()
            pricesInGivenLength["lowPrices$length"] = lowPrices
        }
        for((i,indicator) in indicators.withIndex()){
            combinedIndicators[i] = getIndicatorValue(pricesInGivenLength, indicator)
        }
        return Nd4j.create(combinedIndicators, intArrayOf(1, combinedIndicators.size))
    }

    private fun getIndicatorValue(pricesInGivenLength: HashMap<String, DoubleArray>, indicatorTypeAndLength: Pair<String, Int>): Double {
        val length = indicatorTypeAndLength.second
        val out = DoubleArray(length)
        val begin = MInteger()
        val mutableLength = MInteger()

        var closePrices : DoubleArray? = null
        var highPrices: DoubleArray? = null
        var lowPrices: DoubleArray? = null

        fun initializeClosePricesArray(){
            closePrices =  pricesInGivenLength["closePrices$length"]
        }
        fun initializeHighPricesArray(){
            highPrices = pricesInGivenLength["highPrices$length"]
        }
        fun initializeLowPricesArray(){
            lowPrices = pricesInGivenLength["lowPrices$length"]
        }
        fun initializePricesArrays(){
            initializeHighPricesArray()
            initializeClosePricesArray()
            initializeLowPricesArray()
        }

        when(indicatorTypeAndLength.first) {
            "sma" -> {
                initializeClosePricesArray();
                TALibCore.sma(0, length - 1, closePrices!!, length, begin, mutableLength, out)
            }
            "adx" -> {
                initializePricesArrays()
                TALibCore.adx(0, length - 1, highPrices!!, lowPrices!!, closePrices!!, length / 2, begin, mutableLength, out)
                out[0] = out[0] / 100
            }
            "adxr" -> {
                initializePricesArrays()
                TALibCore.adxr(0, length - 1, highPrices, lowPrices, closePrices, length / 3, begin, mutableLength, out)
                if(mutableLength.value != 0){
                    out[0] = out[mutableLength.value - 1] / 100
                }
                else{
                    out[0] = out[0] / 100
                }
            }
            "ar" -> {
                initializeHighPricesArray()
                initializeLowPricesArray()
                TALibCore.aroonOsc(0, length - 1, highPrices, lowPrices, length - 1, begin, mutableLength, out)
                out[0] = out[0] / 200 + 0.5

            }
            "dx" -> {
                initializePricesArrays()
                TALibCore.dx(0, length - 1, highPrices, lowPrices, closePrices, length - 1 , begin, mutableLength, out)
                out[0] = out[0] / 100
            }
            "mdi" ->{
                initializePricesArrays()
                TALibCore.minusDI(0,length - 1, highPrices, lowPrices, closePrices, length - 1, begin, mutableLength,out)
                out[0] = out[0] / 100
            }
            "pdi" -> {
                initializePricesArrays()
                TALibCore.plusDI(0, length - 1, highPrices, lowPrices, closePrices, length - 1, begin, mutableLength, out)
                out[0] = out[0] / 100
            }
            "rsi" -> {
                initializeClosePricesArray()
                TALibCore.rsi(0, length - 1, closePrices, length - 1, begin, mutableLength, out)
                out[0] = out[0] / 100

            }
            "willr" -> {
                initializePricesArrays()
                TALibCore.willR(0, length - 1, highPrices, lowPrices, closePrices, length, begin, mutableLength, out)
                out[0] =  out[0] / - 100
            }
        }
        return out[0]
    }

    private fun toCandleINDArray(candles: LinkedList<CandleDTO>): Array<CandleDTO?>{
        val candleArray = Array<CandleDTO?>(candles.size, {i -> null})
        for((i,candle) in candles.withIndex())
            candleArray[i] = candle

        return candleArray
    }
}