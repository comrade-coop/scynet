package ai.scynet.protocol

import ai.scynet.evaluator.Evaluator
import ai.scynet.executor.Executor

data class TrainingJob(
    val executor: Executor,
    val trainerClusterGroupName: String,
    val evaluator: Evaluator,
    val egg: Model,
    val dataset: Dataset<*, *>,
    var status: Status
    )