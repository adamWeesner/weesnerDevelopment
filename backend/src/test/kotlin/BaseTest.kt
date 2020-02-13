import io.kotlintest.TestCase
import io.kotlintest.TestResult
import io.kotlintest.specs.AbstractStringSpec
import io.kotlintest.specs.StringSpec
import io.ktor.server.testing.withTestApplication
import java.io.File

open class BaseTest(block: AbstractStringSpec.() -> Unit = {}) : StringSpec() {
    init {
        block(this)
    }

    override fun afterTest(testCase: TestCase, result: TestResult) {
        println("cleanup started")
        withTestApplication {
            environment.stop()
        }
        val db = File("server")
        if (db.isDirectory) {
            val children = db.list()
            children?.indices?.forEach { i ->
                File(db, children[i]).delete()
            }
        }
        println("cleanup ended")
    }
}
