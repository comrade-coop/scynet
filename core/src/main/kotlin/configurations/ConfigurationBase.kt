package ai.scynet.core.configurations

import configurations.ProcessorConfigurationBuilder

import org.apache.ignite.configuration.CacheConfiguration
import org.apache.ignite.configuration.IgniteConfiguration

//TODO: https://discuss.kotlinlang.org/t/make-auto-completion-work-in-kotlin-scripts-with-context/10185/12


open class ConfigurationBase {
	companion object {
		var igniteConfiguration = IgniteConfiguration()
	}
	 fun processors(lambda: ProcessorConfigurationBuilder.() -> Unit): MutableList<ProcessorConfiguration> {
		 return ProcessorConfigurationBuilder().apply(lambda).build()
	 }
	 fun ignite(lambda: IgniteConfiguration.() -> Unit): IgniteConfiguration{
		 igniteConfiguration = IgniteConfiguration().apply(lambda)
		 return igniteConfiguration
	 }
	fun IgniteConfiguration.cache(lamda: CacheConfiguration<*,*>.() -> Unit){
		this.setCacheConfiguration(CacheConfiguration<Any,Any>().apply(lamda))
	}
}