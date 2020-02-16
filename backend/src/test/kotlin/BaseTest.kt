import com.typesafe.config.ConfigFactory
import io.kotlintest.TestCase
import io.kotlintest.TestResult
import io.kotlintest.specs.AbstractStringSpec
import io.kotlintest.specs.StringSpec
import io.ktor.config.HoconApplicationConfig
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.createTestEnvironment
import java.io.File

open class BaseTest(block: AbstractStringSpec.() -> Unit = {}) : StringSpec() {
    companion object {
        val engine = TestApplicationEngine(createTestEnvironment({
            config = HoconApplicationConfig(ConfigFactory.load("application-test.conf"))
        }))
    }

    init {
        block(this)
    }

    override fun beforeTest(testCase: TestCase) {
        engine.start()
    }

    override fun afterTest(testCase: TestCase, result: TestResult) {
        with(engine) {
            environment.stop()
        }
        val db = File("server")
        if (db.isDirectory) {
            val children = db.list()
            children?.indices?.forEach { i ->
                File(db, children[i]).delete()
            }
        }
    }
}
