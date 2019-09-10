package ai.scynet.protocol

sealed class Status{
    abstract val statusID: StatusID
    abstract val results: HashMap<String, String>
}

data class UNTRAINED(val res: HashMap<String, String>): Status(){
    override val statusID: StatusID = StatusID.UNTRAINED
    override val results = res
}

data class TRAINED(val res: HashMap<String, String>) : Status(){
    override val statusID: StatusID = StatusID.TRAINED
    override val results = res
}

// TODO: Not sure about this one. Discuss?
data class VALIDATED(val res: HashMap<String, String>): Status(){
    override val statusID: StatusID = StatusID.VALIDATED
    override val results = res
}

