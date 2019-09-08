package ai.scynet.protocol

sealed class Status{
   abstract val statusID: StatusID
}
class UNTRAINED: Status(){
    override val statusID: StatusID = StatusID.UNTRAINED
}

data class TRAINED(val results: HashMap<String, String>) : Status(){
    override val statusID: StatusID = StatusID.TRAINED
}

// TODO: Not sure about this one. Discuss?
data class VALIDATED(val loss: Double): Status(){
    override val statusID: StatusID = StatusID.VALIDATED
}

