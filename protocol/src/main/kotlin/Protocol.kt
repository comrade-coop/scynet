package ai.scynet.protocol

import ai.scynet.common.registry.JobRegistry
import ai.scynet.common.registry.Registry

interface Protocol {
    val jobRegistry: JobRegistry<*>
    val datasetRegistry: Registry<*, Dataset<*,*>>
}