package generator

import com.weesnerdevelopment.test.utils.shouldBe
import generator.classes.GeneratorFile
import org.junit.jupiter.api.Test

class GeneratorFileTests {
    @Test
    fun `can save file data`() {
        runCatching {
            GeneratorFile(
                path = "random/Path/Here/name",
                data = ""
            ).save {
                "random data here"
            }
        }.isSuccess shouldBe true
    }
}
