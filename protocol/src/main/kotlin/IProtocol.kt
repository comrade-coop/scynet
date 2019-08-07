package ai.scynet.protocol

interface IProtocol<K> {
    fun addJob(key: K, trainingJob: TrainingJob<*,*>)

    fun queryJobs(predicate: (K, TrainingJob<*,*>) -> Boolean, callback: (K, TrainingJob<*,*>) -> Unit): Cursor<K,TrainingJob<*,*>>

    fun addDataset(key: K, dataset: Dataset<*,*>)

    fun queryDataset(predicate: (K, Dataset<*,*>) -> Boolean, callback: (K, Dataset<*,*>) -> Unit): Cursor<K,Dataset<*,*>>
}