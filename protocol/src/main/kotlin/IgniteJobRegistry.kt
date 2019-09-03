package ai.scynet.protocol

import JobRegistry
import ai.scynet.core.common.registry.IgniteRegistry
import common.registry.exceptions.TrainingJobDoesNotExistException
import common.registry.exceptions.TrainingJobExistsException
import org.apache.ignite.IgniteCache

class IgniteJobRegistry<K>(name:String): IgniteRegistry<K, TrainingJob<*, *>>(name), JobRegistry<K> {
    var takenJobs: IgniteCache<K, Boolean>
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

    override fun setJob(key: K, trainedJob: TrainingJob<*,*>){
        checkTrainingJobExists(key)
        cache.put(key, trainedJob)
    }

    private fun checkTrainingJobExists(key: K){
        if(!cache.containsKey(key)){
            throw TrainingJobDoesNotExistException("Training job with key = ${key} does not exist")
        }
    }
}