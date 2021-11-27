package generator

import com.weesnerdevelopment.test.utils.shouldBe
import generator.classes.Template
import kotlin.test.Test

class TemplateTests {
    @Test
    fun `update template data with all required fields succeeds`() {
        Template(
            name = "buildGradle",
            data = "#a is to #b as #b is to #a"
        ).update {
            listOf(
                "#a" to "apple",
                "#b" to "orange"
            )
        } shouldBe "apple is to orange as orange is to apple"
    }

    @Test
    fun `update template data with missing required fields throws`() {
        runCatching {
            Template(
                name = "buildGradle",
                data = "#a is to #b as #b is to #a"
            ).update {
                listOf(
                    "#a" to "apple"
                )
            }
        }.exceptionOrNull()?.message shouldBe "Template still has arguments to override: #b"
    }
}
