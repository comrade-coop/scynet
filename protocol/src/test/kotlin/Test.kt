import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class MyTests : StringSpec({
    "strings.length should return size of string" {
      "protocol".length shouldBe 8
    }
})
