import auth.TokenResponse
import com.typesafe.config.ConfigFactory
import com.weesnerdevelopment.Path
import io.kotlintest.TestCase
import io.kotlintest.TestResult
import io.kotlintest.specs.AbstractStringSpec
import io.kotlintest.specs.StringSpec
import io.ktor.config.HoconApplicationConfig
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.createTestEnvironment
import shared.auth.HashedUser
import shared.auth.User
import shared.fromJson
import shared.toJson
import java.io.File

open class BaseTest(block: AbstractStringSpec.(token: String) -> Unit = {}) : StringSpec() {
    companion object {
        val engine = TestApplicationEngine(createTestEnvironment {
            config = HoconApplicationConfig(ConfigFactory.load("application-test.conf"))
        })
    }

    init {
        var token: String

        engine.start()

        with(engine) {
            val request = bodyRequest(
                Post,
                Path.User.base + Path.User.signUp,
                User(
                    name = "test",
                    email = "test@email.com",
                    username = "test",
                    password = "test"
                ).toJson()
            )

            token = request.response.content?.fromJson<TokenResponse>()?.token
                ?: bodyRequest(
                    Post,
                    Path.User.base + Path.User.login,
                    HashedUser("test", "test").toJson()
                ).response.content?.fromJson<TokenResponse>()?.token
                        ?: throw IllegalArgumentException("Something happened... should have gotten a token")
        }

        this.block(token)
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
