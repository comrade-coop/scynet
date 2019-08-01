package ai.scynet.protocol

sealed class Status
object UNTRAINED : Status()
data class TRAINED(val prediction: Double) : Status()
data class VALIDATED(val loss: Double): Status()

