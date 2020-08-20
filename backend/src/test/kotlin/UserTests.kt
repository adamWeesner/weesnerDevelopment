import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpStatusCode
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import shared.auth.*
import java.util.*
import kotlin.random.Random

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserTests : BaseTest() {
    val newUser
        get() = User(
            name = "Adam",
            email = "randomemail${Random.nextInt(999)}@email.com",
            username = "adam.weesner.${Random.nextInt(999)}",
            password = "password"
        )

    private fun newUserHashed(name: String) = newUser.copy(
        username = Base64.getEncoder().encodeToString(name.toByteArray()),
        password = Base64.getEncoder().encodeToString(newUser.password?.toByteArray())
    )

    val path = Path.User.base

    @Test
    @Order(1)
    fun `access account without login gives InvalidJwt`() {
        BuiltRequest(engine, Get, path + Path.User.account)
            .asServerError<Unit, InvalidUserException>().reasonCode shouldBe InvalidUserReason.InvalidJwt.code
    }

    @Test
    @Order(2)
    fun `login with non-encrypted data gives InvalidUserInfo`() {
        BuiltRequest(engine, Post, path + Path.User.login)
            .asServerError<HashedUser, InvalidUserException>(newUser.asHashed()).reasonCode shouldBe InvalidUserReason.InvalidUserInfo.code
    }

    @Test
    @Order(3)
    fun `sign up with non-encrypted data gives InvalidUserInfo`() {
        BuiltRequest(engine, Post, path + Path.User.signUp)
            .asServerError<User, InvalidUserException>(newUser).reasonCode shouldBe InvalidUserReason.InvalidUserInfo.code
    }

    @Test
    @Order(4)
    fun `login with user data that does not exist gives NoUserFound`() {
        BuiltRequest(engine, Post, path + Path.User.login)
            .asServerError<HashedUser, InvalidUserException>(newUserHashed("doggo").asHashed()).reasonCode shouldBe InvalidUserReason.NoUserFound.code
    }

    @Test
    @Order(5)
    fun `sign up gives Created`() {
        BuiltRequest(engine, Post, path + Path.User.signUp)
            .sendStatus(newUserHashed("bob")) shouldBe HttpStatusCode.Created
    }

    @Test
    @Order(6)
    fun `login with created user gives OK`() {
        val signedUp = newUserHashed("catto")
        BuiltRequest(engine, Post, path + Path.User.signUp).send(signedUp)
        BuiltRequest(engine, Post, path + Path.User.login).sendStatus(signedUp.asHashed()) shouldBe HttpStatusCode.OK
    }

    @Test
    @Order(7)
    fun `get account info with created user gives user data`() {
        val signedUp = newUserHashed("adam")
        BuiltRequest(engine, Post, path + Path.User.signUp).send(signedUp)
        val authToken = BuiltRequest(engine, Post, path + Path.User.login)
            .asClass<HashedUser, TokenResponse>(signedUp.asHashed())?.token

        BuiltRequest(engine, Get, path + Path.User.account, authToken)
            .asObject<User>().copy(uuid = null, dateCreated = -1, dateUpdated = -1) shouldBe signedUp.copy(
            password = null,
            dateCreated = -1,
            dateUpdated = -1
        )
    }
}
