package harvester.normalization

import org.apache.ignite.services.ServiceContext
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import processors.LazyStreamService

class NormalizingService: LazyStreamService<Long, INDArray>() {
    override fun execute(ctx: ServiceContext?) {
        super.execute(ctx)
        inputStreams[0].listen{timestamp: Long, window: INDArray, _ ->
            val minAndDifferenceRowVectors = getMinAndDifferenceRowVectors(window)
            window.subiRowVector(minAndDifferenceRowVectors.first)
            window.diviRowVector(minAndDifferenceRowVectors.second)
            cache.put(timestamp, window)
        }
    }

    private fun getMinAndDifferenceRowVectors(window: INDArray): Pair<INDArray, INDArray>{
        val columns = window.shape()[1].toInt()
        val mins = DoubleArray(columns)
        val minMaxDifferences = DoubleArray(columns)
        for(columnIndex  in 0 until columns){
            val column = window.getColumn(columnIndex.toLong())

            val min = column.minNumber() as Double
            mins[columnIndex] = min

            val max = column.maxNumber() as Double
            var difference = max - min
            if(difference == 0.0)
                difference = 1.0
            minMaxDifferences[columnIndex] = difference
        }

        return Pair(Nd4j.create(mins, intArrayOf(1, columns)), Nd4j.create(minMaxDifferences, intArrayOf(1, columns)))
    }

}