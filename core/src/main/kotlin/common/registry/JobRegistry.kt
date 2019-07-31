package ai.scynet.common.registry

import ai.scynet.protocol.Status
import ai.scynet.protocol.TrainingJob
import common.registry.exceptions.TrainingJobDoesNotExistException
import common.registry.exceptions.TrainingJobExistsException
import org.apache.ignite.IgniteCache

class JobRegistry<K>:IgniteRegistry<K, TrainingJob<*,*>>("JobRegistry"){
    lateinit var takenJobs: IgniteCache<K, *>
    init {
        cache = ignite.getOrCreateCache<K,TrainingJob<*,*>>("jobRegistry")
        takenJobs = ignite.getOrCreateCache<K, Boolean>("takenJobs")
    }
    override fun put(key: K, value: TrainingJob<*,*>) {
        if(cache.putIfAbsent(key, value)){
            println("TrainingJob with key = ${key} successfully added!")
            return
        }
        throw TrainingJobExistsException("Training job with key = ${key} already exists")
    }

    fun jobTrained(key: K, trainedJob: TrainingJob<*,*>){
        checkTrainingJobExists(key)
        trainedJob.status = Status.TRAINED
        cache.put(key, trainedJob)
    }

    fun jobValidated(key: K, trainedJob: TrainingJob<*, *>, loss: Float) {
        checkTrainingJobExists(key)
        trainedJob.status = Status.VALIDATED
        trainedJob.loss = loss
        cache.put(key, trainedJob)
    }

    private fun checkTrainingJobExists(key: K){
        if(!cache.containsKey(key)){
            throw TrainingJobDoesNotExistException("Training job with key = ${key} does not exist")
        }
    }
}