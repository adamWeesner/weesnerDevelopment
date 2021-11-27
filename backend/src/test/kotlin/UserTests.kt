import com.weesnerdevelopment.shared.auth.*
import com.weesnerdevelopment.test.utils.BaseTest
import com.weesnerdevelopment.test.utils.shouldBe
import io.ktor.http.*
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*
import kotlin.random.Random

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserTests : BaseTest("application-test.conf") {
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
        get(path + Path.User.account, usingToken = null)
            .asServerError<Unit, InvalidUserException>().reasonCode shouldBe InvalidUserReason.General.code
    }

    @Test
    @Order(2)
    fun `login with non-encrypted data gives InvalidUserInfo`() {
        post(path + Path.User.login, usingToken = null)
            .asServerError<HashedUser, InvalidUserException>(newUser.asHashed()).reasonCode shouldBe InvalidUserReason.InvalidUserInfo.code
    }

    @Test
    @Order(3)
    fun `sign up with non-encrypted data gives InvalidUserInfo`() {
        post(path + Path.User.signUp, usingToken = null)
            .asServerError<User, InvalidUserException>(newUser).reasonCode shouldBe InvalidUserReason.InvalidUserInfo.code
    }

    @Test
    @Order(4)
    fun `login with user data that does not exist gives NoUserFound`() {
        post(path + Path.User.login, usingToken = null)
            .asServerError<HashedUser, InvalidUserException>(newUserHashed("doggo").asHashed()).reasonCode shouldBe InvalidUserReason.NoUserFound.code
    }

    @Test
    @Order(5)
    fun `sign up gives Created`() {
        post(path + Path.User.signUp, usingToken = null)
            .sendStatus(newUserHashed("bob")) shouldBe HttpStatusCode.Created
    }

    @Test
    @Order(6)
    fun `login with created user gives OK`() {
        val signedUp = newUserHashed("catto")
        post(path + Path.User.signUp, usingToken = null).send(signedUp)
        post(path + Path.User.login, usingToken = null).sendStatus(signedUp.asHashed()) shouldBe HttpStatusCode.OK
    }

    @Test
    @Order(7)
    fun `get account info with created user gives user data`() {
        val signedUp = newUserHashed("adam")
        post(path + Path.User.signUp, usingToken = null).send(signedUp)
        val authToken = post(path + Path.User.login, usingToken = null)
            .asClass<HashedUser, TokenResponse>(signedUp.asHashed())?.token

        get(path + Path.User.account, usingToken = authToken).asObject<User>()
            .copy(uuid = null, dateCreated = -1, dateUpdated = -1) shouldBe signedUp.copy(
            password = null,
            dateCreated = -1,
            dateUpdated = -1
        )
    }
}
