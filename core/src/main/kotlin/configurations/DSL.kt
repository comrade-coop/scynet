package configurations

import ai.scynet.core.configurations.ProcessorConfiguration
import org.apache.ignite.configuration.IgniteConfiguration

fun ignite(lambda: IgniteConfigurationBuilder.() -> Unit): IgniteConfiguration = IgniteConfigurationBuilder().apply(lambda).build()

fun processors(lambda: ProcessorConfigurationBuilder.() -> Unit): MutableList<ProcessorConfiguration> = ProcessorConfigurationBuilder().apply(lambda).build()
