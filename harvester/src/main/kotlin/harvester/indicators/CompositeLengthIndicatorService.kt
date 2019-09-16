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
                val out = getIndicatorsValue(toCandlArray(candles))
                cache.put(timestamp, out)
                candles.removeFirst()
            }
        }
    }

    private fun getIndicatorsValue(candles: Array<CandleDTO?>): INDArray {
        val combinedIndicators = DoubleArray(indicators.size)
        for((i,indicator) in indicators.withIndex()){
            combinedIndicators[i] = getIndicatorValue(candles, indicator)
        }
        return Nd4j.create(combinedIndicators, intArrayOf(1, combinedIndicators.size))
    }

    private fun getIndicatorValue(candles: Array<CandleDTO?>, indicatorTypeAndLength: Pair<String, Int>): Double {
        val length = indicatorTypeAndLength.second
        val currentCandles = candles.copyOfRange(candles.size - length , candles.size)
        val out = DoubleArray(length)
        val begin = MInteger()
        val mutableLength = MInteger()

        var closePrices : DoubleArray? = null
        var highPrices: DoubleArray? = null
        var lowPrices: DoubleArray? = null

        fun initializeClosePricesArray(){
            closePrices =  currentCandles.map { it!!.close }.toDoubleArray()
        }
        fun initializeHighPricesArray(){
            highPrices = currentCandles.map { it!!.high }.toDoubleArray()
        }
        fun initializeLowPricesArray(){
            lowPrices = currentCandles.map { it!!.high }.toDoubleArray()
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
                TALibCore.adx(0, length - 1, highPrices!!, lowPrices!!, closePrices!!, length, begin, mutableLength, out)
                out[0] = out[0] / 100
            }
            "adxr" -> {
                initializePricesArrays()
                TALibCore.adxr(0, length - 1, highPrices, lowPrices, closePrices, length, begin, mutableLength, out)
                out[0] = out[0] / 100
            }
            "ar" -> {
                initializeHighPricesArray()
                initializeLowPricesArray()
                TALibCore.aroonOsc(0, length - 1, highPrices, lowPrices, length, begin, mutableLength, out)
                out[0] = out[0] / 200 + 0.5

            }
            "dx" -> {
                initializePricesArrays()
                TALibCore.dx(0, length - 1, highPrices, lowPrices, closePrices, length, begin, mutableLength, out)
                out[0] = out[0] / 100
            }
            "mdi" ->{
                initializeClosePricesArray()
                TALibCore.minusDI(0,length - 1, highPrices, lowPrices, closePrices, length, begin, mutableLength,out)
                out[0] = out[0] / 100
            }
            "pdi" -> {
                initializePricesArrays()
                TALibCore.plusDI(0, length - 1, highPrices, lowPrices, closePrices, length, begin, mutableLength, out)
                out[0] = out[0] / 100
            }
            "rsi" -> {
                initializeClosePricesArray()
                TALibCore.rsi(0, length - 1, closePrices, length, begin, mutableLength, out)
                out[0] = out[0] / 100

            }
            "willr" -> {
                initializePricesArrays()
                TALibCore.willR(0, length - 1, highPrices, lowPrices, closePrices, length, begin, mutableLength, out)
                out[0] = out[0] / -100

            }



        }
        return out[0]
    }

    private fun toCandlArray(candles: LinkedList<CandleDTO>): Array<CandleDTO?>{
        val candleArray = Array<CandleDTO?>(candles.size, {i -> null})
        for((i,candle) in candles.withIndex())
            candleArray[i] = candle

        return candleArray
    }
}