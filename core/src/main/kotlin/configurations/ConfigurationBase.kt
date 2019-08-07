package ai.scynet.core.configurations

import org.apache.ignite.configuration.CacheConfiguration
import org.apache.ignite.configuration.IgniteConfiguration
import kotlin.collections.ArrayList

//TODO: https://discuss.kotlinlang.org/t/make-auto-completion-work-in-kotlin-scripts-with-context/10185/12


open class ConfigurationBase {
	companion object {
		var igniteConfiguration = IgniteConfiguration()
		var processorConfigurations: MutableList<ProcessorConfiguration> = mutableListOf()
	}
	 fun processors(lambda: ProcessorConfigurationBuilder.() -> Unit): MutableList<ProcessorConfiguration> {
		 processorConfigurations = ProcessorConfigurationBuilder().apply(lambda).build()
		 return processorConfigurations
	 }

	 fun ignite(lambda: IgniteConfiguration.() -> Unit): IgniteConfiguration{
		 igniteConfiguration = IgniteConfiguration().apply(lambda)
		 return igniteConfiguration
	}
	fun IgniteConfiguration.cache(lamda: CacheConfiguration<*,*>.() -> Unit){
		this.setCacheConfiguration(CacheConfiguration<Any,Any>().apply(lamda))
	}
}