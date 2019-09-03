package ai.scynet.protocol

import ai.scynet.core.common.registry.cursors.Cursor
import ai.scynet.protocol.exceptions.JobNotAvailableException

interface IProtocol<K> {
    fun addJob(key: K, trainingJob: TrainingJob<*,*>)

    fun queryJobs(predicate: (K, TrainingJob<*,*>) -> Boolean, callback: (K, TrainingJob<*,*>) -> Unit): Cursor<K, TrainingJob<*, *>>

    fun addDataset(key: K, dataset: Dataset<*,*>)

    fun queryDataset(predicate: (K, Dataset<*,*>) -> Boolean, callback: (K, Dataset<*,*>) -> Unit): Cursor<K,Dataset<*,*>>

    @Throws(JobNotAvailableException::class)
    fun takeJob(key: K)
}