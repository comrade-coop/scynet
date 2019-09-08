package ai.scynet.protocol

import ai.scynet.evaluator.Evaluator
import ai.scynet.executor.Executor
import org.nd4j.linalg.api.ndarray.INDArray
import java.util.*

data class TrainingJob(
        val UUID: UUID,
        val executor: String, // URI or something
        val trainerClusterGroupName: String?,
        val evaluator: String, // URI or something,
        val egg: String, // class Model
        val dataset: HashMap<String, INDArray>?, // "x": INDArray, "y": INDArray
        var status: Status // class Status
)