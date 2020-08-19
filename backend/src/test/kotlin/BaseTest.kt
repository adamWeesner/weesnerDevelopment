import com.typesafe.config.ConfigFactory
import com.weesnerdevelopment.utils.Path
import io.ktor.config.HoconApplicationConfig
import io.ktor.http.HttpMethod
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.createTestEnvironment
import io.ktor.util.KtorExperimentalAPI
import kimchi.Kimchi
import org.junit.jupiter.api.*
import shared.auth.HashedUser
import shared.auth.TokenResponse
import shared.auth.User
import java.io.File

@KtorExperimentalAPI
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class BaseTest {
    val engine = TestApplicationEngine(createTestEnvironment {
        config = HoconApplicationConfig(ConfigFactory.load("application-test.conf"))
    })

    var token: String = ""
    lateinit var signedInUser: User

    fun deleteDb() {
        val db = File("server")
        if (db.isDirectory) {
            val children = db.list()
            children?.indices?.forEach { i ->
                Kimchi.debug("Removing old server files... ${children[i]}")
                File(db, children[i]).delete()
            }
        }
    }

    fun createUser() {
        token =
            BuiltRequest(engine, Post, Path.User.base + Path.User.signUp).asClass<User, TokenResponse>(
                User(
                    name = "test",
                    email = "test@email.com",
                    username = "test",
                    password = "test"
                )
            )?.token ?: BuiltRequest(engine, Post, Path.User.base + Path.User.login)
                .asClass<HashedUser, TokenResponse>(HashedUser("test", "test"))?.token
                    ?: throw IllegalArgumentException("Something happened... should have gotten a token")


        signedInUser = BuiltRequest(engine, HttpMethod.Get, "${Path.User.base}${Path.User.account}", token).asObject()
    }

    @BeforeAll
    fun setup() {
        deleteDb()

        engine.start()
        createUser()
    }

    @AfterAll
    fun tearDown() {
        engine.environment.stop()
    }
}
