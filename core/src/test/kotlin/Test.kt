import ai.scynet.core.descriptors.ProcessorDescriptor
import ai.scynet.core.processors.Processor
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.net.URI

class ProcessorDescriptorTest : StringSpec({
    "Check if the processor is being created properly" {
        val processor = ProcessorDescriptor.fromURI(URI.create("java.lang.String://alex4o?hello=world&thing=12#coindig"))
        processor.properties["hello"] shouldBe "world"
        processor.properties["thing"] shouldBe "12"
        println(processor)
    }
})
