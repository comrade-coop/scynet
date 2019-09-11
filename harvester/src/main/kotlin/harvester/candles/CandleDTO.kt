package harvester.candles

data class CandleDTO (val open: Double, val close: Double, val high: Double, val low: Double, val timestamp: Long){
    fun getData(): DoubleArray{
        return doubleArrayOf(open, close, high, low)
    }
}