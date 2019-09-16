package harvester.datasets

import org.apache.ignite.IgniteCache
import org.apache.ignite.services.ServiceContext
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import processors.LazyStreamService
import java.util.*

class DatasetService: LazyStreamService<String, Pair<INDArray,INDArray>>() {
    private lateinit var outputInputCache: IgniteCache<String, LinkedList<Pair<INDArray,INDArray>>>
    private  var datasetSize: Int? = null
    // window slide; 1<= slide <= datasetSize; if slide = datasetSize --> tumbling window
    private var slide: Int? = null
    private val OUTPUT_INPUT: String = "outputInputLinkedList"
    private val LATEST_DATASET: String = "latestDataset"

    override fun init(ctx: ServiceContext?) {
        super.init(ctx)
        outputInputCache = ignite.getOrCreateCache("outputInput")
        datasetSize = descriptor!!.properties!!.get("datasetSize") as Int
        slide = descriptor!!.properties!!.get("slide") as Int
    }

    override fun execute(ctx: ServiceContext?) {
        super.execute(ctx)
        if(!outputInputCache.containsKey(OUTPUT_INPUT))
            outputInputCache.put(OUTPUT_INPUT, LinkedList())
        inputStreams[0].listen{ timestamp: Long, pair: Pair<Boolean, INDArray>, _ ->
            var label: INDArray
            if(pair.first){
                label = Nd4j.ones(1)
            }else{
                label = Nd4j.zeros(1)
            }

            val inputShape = pair.second.shape()
            for(s in inputShape){
                println(s)
            }
            val input = pair.second.reshape(1, inputShape[0], inputShape[1])
            println("\n --------- after ------ \n $input \n")

            val inputOutputs = outputInputCache.get(OUTPUT_INPUT)
            inputOutputs.addLast(Pair(label,input))

            if(inputOutputs.size == datasetSize){
                updateDataset(inputOutputs)
                repeat(slide!!){
                    inputOutputs.removeFirst()
                }

            }

            outputInputCache.put(OUTPUT_INPUT, inputOutputs)
        }
    }

    private fun updateDataset(inputOutputs: LinkedList<Pair<INDArray,INDArray>>){
        var inputs = inputOutputs.first.second
        var labels = inputOutputs.first.first
        for((i,inputOutput) in inputOutputs.withIndex()){
            if(i == 0)
                continue
            labels = Nd4j.vstack(labels, inputOutput.first)
            inputs = Nd4j.vstack(inputs,inputOutput.second)
        }
        cache.put(LATEST_DATASET, Pair(labels, inputs))
    }
}