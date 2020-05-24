import auth.TokenResponse
import com.typesafe.config.ConfigFactory
import com.weesnerdevelopment.utils.Path
import io.kotlintest.Spec
import io.kotlintest.TestCase
import io.kotlintest.specs.AbstractStringSpec
import io.kotlintest.specs.StringSpec
import io.ktor.config.HoconApplicationConfig
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.createTestEnvironment
import shared.auth.HashedUser
import shared.auth.User
import java.io.File

open class BaseTest(block: AbstractStringSpec.(token: String) -> Unit = {}, usesToken: Boolean = true) : StringSpec() {
    private var started = false

    companion object {
        val engine = TestApplicationEngine(createTestEnvironment {
            config = HoconApplicationConfig(ConfigFactory.load("application-test.conf"))
        })
    }

    init {
        var token = ""

        val db = File("server")
        if (db.isDirectory) {
            val children = db.list()
            children?.indices?.forEach { i ->
                println("Removing old server files...")
                File(db, children[i]).delete()
            }
        }

        engine.start()
        if (usesToken) {
            with(engine) {
                token = BuiltRequest(this, Post, Path.User.base + Path.User.signUp).asClass<User, TokenResponse>(
                    User(
                        name = "test",
                        email = "test@email.com",
                        username = "test",
                        password = "test"
                    )
                ).token
                    ?: BuiltRequest(this, Post, Path.User.base + Path.User.login)
                        .asClass<HashedUser, TokenResponse>(HashedUser("test", "test")).token
                            ?: throw IllegalArgumentException("Something happened... should have gotten a token")
            }
        }

        block(token)
    }

    override fun beforeTest(testCase: TestCase) {
        if (!started) {
            started = true
            engine.start()
        }
    }

    override fun afterSpec(spec: Spec) {
        super.afterSpec(spec)
        engine.environment.stop()
    }
}
