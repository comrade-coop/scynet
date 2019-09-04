package ai.scynet.protocol

sealed class Status{
   abstract val statusID: StatusID
}
class UNTRAINED: Status(){
    override val statusID: StatusID = StatusID.UNTRAINED
}
data class TRAINED(val prediction: Double) : Status(){
    override val statusID: StatusID = StatusID.TRAINED
}
data class VALIDATED(val loss: Double): Status(){
    override val statusID: StatusID = StatusID.VALIDATED
}

