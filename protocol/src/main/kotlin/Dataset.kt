package ai.scynet.protocol

import org.tensorflow.Tensor
import java.util.concurrent.Future

interface Dataset <X, Y> {
    val x: Tensor<X>
    val y: Tensor<Y>
    val xTest: Future<Tensor<X>>?
    val yTest: Future<Tensor<Y>>?
}