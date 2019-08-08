package common.registry

import ai.scynet.common.registry.Registry
import ai.scynet.protocol.TrainingJob

interface JobRegistry<K>: Registry<K, TrainingJob<*,*>> {
    fun setJob(key: K, trainedJob: TrainingJob<*,*>)
}