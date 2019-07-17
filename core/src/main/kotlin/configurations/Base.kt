package ai.scynet.core.configurations

import configurations.IgniteConfigurationBuilder
import configurations.ProcessorConfigurationBuilder

import ai.scynet.core.configurations.ProcessorConfiguration
import org.apache.ignite.configuration.IgniteConfiguration



open class Base {
	companion object {
		var igniteConfiguration = IgniteConfiguration()
		var igniteCacheConfiguration: MutableList<ProcessorConfiguration> = mutableListOf()
	}

	fun ignite(lambda: IgniteConfigurationBuilder.() -> Unit) {
		igniteConfiguration = IgniteConfigurationBuilder().apply(lambda).build()
	}

	fun processors(lambda: ProcessorConfigurationBuilder.() -> Unit): MutableList<ProcessorConfiguration> {
		return ProcessorConfigurationBuilder().apply(lambda).build()
	}
}