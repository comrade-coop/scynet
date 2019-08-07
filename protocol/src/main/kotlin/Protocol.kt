package ai.scynet.protocol

import ai.scynet.common.registry.Registry
import common.registry.JobRegistry

abstract class Protocol<K>(): IProtocol<K> {
    protected abstract val jobRegistry: JobRegistry<K>

    protected abstract val datasetRegistry: Registry<K, Dataset<*, *>>

    override fun addJob(key: K, trainingJob: TrainingJob<*, *>) {
        when(trainingJob.status.statusID){
            StatusID.UNTRAINED -> jobRegistry.put(key, trainingJob)
            StatusID.TRAINED, StatusID.VALIDATED -> jobRegistry.setJob(key, trainingJob)
        }
    }

    override fun queryJobs(predicate: (K, TrainingJob<*,*>) -> Boolean, callback: (K, TrainingJob<*,*>) -> Unit): Cursor<K,TrainingJob<*,*>> {
        return jobRegistry.query(predicate, callback)
    }

    override fun addDataset(key: K, dataset: Dataset<*,*>){
        datasetRegistry.put(key, dataset)
    }

    override fun queryDataset(predicate: (K, Dataset<*,*>) -> Boolean, callback: (K, Dataset<*,*>) -> Unit): Cursor<K,Dataset<*,*>>{
        return datasetRegistry.query(predicate, callback)
    }
}