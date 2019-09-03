package ai.scynet.protocol

import ai.scynet.common.registry.Registry
import ai.scynet.protocol.exceptions.JobNotAvailableException
import JobRegistry
import ai.scynet.core.common.registry.cursors.Cursor


abstract class Protocol<K>(): IProtocol<K> {
    protected abstract val jobRegistry: JobRegistry<K>

    protected abstract val datasetRegistry: Registry<K, Dataset<*, *>>

    protected abstract val validatedJobRegistry: JobRegistry<K>

    protected abstract val jobAvailabilityRegistry: Registry<K, Boolean>

    override fun addJob(key: K, trainingJob: TrainingJob) {
        when(trainingJob.status.statusID){
            StatusID.UNTRAINED -> {
                jobRegistry.put(key, trainingJob)
                jobAvailabilityRegistry.put(key, true)
            }
            StatusID.TRAINED -> jobRegistry.setJob(key, trainingJob)
            StatusID.VALIDATED -> {
                validatedJobRegistry.put(key, trainingJob)
                jobRegistry.delete(key)
                jobAvailabilityRegistry.delete(key)
            }
        }
    }

    override fun queryJobs(predicate: (K, TrainingJob) -> Boolean, callback: (K, TrainingJob) -> Unit): Cursor<K, TrainingJob> {
        return  jobRegistry.query(predicate, callback)
    }

    override fun addDataset(key: K, dataset: Dataset<*,*>){
        datasetRegistry.put(key, dataset)
    }

    override fun queryDataset(predicate: (K, Dataset<*,*>) -> Boolean, callback: (K, Dataset<*,*>) -> Unit): Cursor<K,Dataset<*,*>>{
        return datasetRegistry.query(predicate, callback)
    }

    override fun takeJob(key: K){
        if(jobAvailabilityRegistry.get(key) == false)
            throw JobNotAvailableException("Job with key = ${key} is already taken!")
        jobAvailabilityRegistry.put(key, false)
    }
}