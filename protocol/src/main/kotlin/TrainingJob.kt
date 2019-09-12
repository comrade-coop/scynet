package ai.scynet.protocol

import org.nd4j.linalg.api.ndarray.INDArray
import java.util.*
import kotlin.collections.HashMap


data class TrainingJob(
		val UUID: UUID,
		val executor: String, // URI or something
		val trainerClusterGroupName: String?,
		val evaluator: String, // URI or something,
		val egg: String, // class Model
		val dataset: HashMap<String, INDArray>?, // "x": INDArray, "y": INDArray
		var status: Status // class Status
)