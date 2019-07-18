import ai.scynet.core.configurations.ConfigurationBase
import ai.scynet.core.processors.BasicProcessor
import ai.scynet.core.processors.Processor
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.util.*
import kotlin.reflect.full.isSubclassOf

class DSLProcessorConfigurationTest: StringSpec(){
    companion object {
        val processorConfigurations = ConfigurationBase().processors {
            processor {
                problem = "Problem"
                processorClass = BasicProcessor::class
                inputs = mutableListOf("There", "are", "some", "problems")
                properties = Properties()
            }
            processor {
                problem = "No Problem"
                processorClass = BasicProcessor::class
                inputs = mutableListOf("There", "are", "no", "problems")
                properties = Properties()
            }
        }
        val configProblem = processorConfigurations[0]
        val configNoProblem = processorConfigurations[1]
    }

    init {
        "Processor names should be as expected"{
            configProblem.problem shouldBe "Problem"
            configNoProblem.problem shouldBe "No Problem"
        }

        "Processor Class should use the Processor interface"{
            var isSubtype =  configProblem.processorClass.isSubclassOf(Processor::class)
            isSubtype shouldBe true

            isSubtype = configNoProblem.processorClass.isSubclassOf(Processor::class)
            isSubtype shouldBe true
        }

        "Inputs must match"{
            configProblem.inputs shouldBe mutableListOf("There","are","some","problems")
            configNoProblem.inputs shouldBe  mutableListOf("There","are","no","problems")
        }

        "Properties must match"{
            val properties = Properties()
            configProblem.properties shouldBe properties
            configProblem.properties shouldBe properties
        }
    }
}