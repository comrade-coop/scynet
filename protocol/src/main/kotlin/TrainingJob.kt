package ai.scynet.protocol

import ai.scynet.evaluator.Evaluator
import ai.scynet.executor.Executor
import ai.scynet.trainer.Trainer

interface TrainingJob<X, Y> {
    val executor: Executor
    val trainer: Trainer
    val evaluator: Evaluator
    val egg: Model
    val dataset: Dataset<X, Y>
    var status: Status
    var loss: Float?
}