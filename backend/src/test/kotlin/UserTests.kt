import com.weesnerdevelopment.utils.Path
import io.kotlintest.shouldBe
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpStatusCode
import shared.auth.*
import java.util.*

class UserTests : BaseTest({
    val newUser = User(
        name = "Adam",
        email = "randomemail@email.com",
        username = "adam.weesner",
        password = "password"
    )

    fun newUserHashed(name: String) = newUser.copy(
        username = Base64.getEncoder().encodeToString(name.toByteArray()),
        password = Base64.getEncoder().encodeToString(newUser.password?.toByteArray())
    )

    val path = Path.User.base

    "access account without login gives InvalidJwt" {
        BuiltRequest(engine, Get, path + Path.User.account)
            .asServerError<Unit, InvalidUserException>().reasonCode shouldBe InvalidUserReason.InvalidJwt.code
    }

    "login with non-encrypted data gives InvalidUserInfo" {
        BuiltRequest(engine, Post, path + Path.User.login)
            .asServerError<HashedUser, InvalidUserException>(newUser.asHashed()).reasonCode shouldBe InvalidUserReason.InvalidUserInfo.code
    }

    "sign up with non-encrypted data gives InvalidUserInfo" {
        BuiltRequest(engine, Post, path + Path.User.signUp)
            .asServerError<User, InvalidUserException>(newUser).reasonCode shouldBe InvalidUserReason.InvalidUserInfo.code
    }

    "login with user data that does not exist gives NoUserFound" {
        BuiltRequest(engine, Post, path + Path.User.login)
            .asServerError<HashedUser, InvalidUserException>(newUserHashed("doggo").asHashed()).reasonCode shouldBe InvalidUserReason.NoUserFound.code
    }

    "sign up gives Created" {
        BuiltRequest(engine, Post, path + Path.User.signUp)
            .sendStatus(newUserHashed("bob")) shouldBe HttpStatusCode.Created
    }

    "login with created user gives OK" {
        BuiltRequest(engine, Post, path + Path.User.signUp).send(newUserHashed("catto"))
        BuiltRequest(engine, Post, path + Path.User.login)
            .sendStatus(newUserHashed("catto").asHashed()) shouldBe HttpStatusCode.OK
    }

    "get account info with created user gives user data" {
        BuiltRequest(engine, Post, path + Path.User.signUp).send(newUserHashed("adam"))
        val authToken = BuiltRequest(engine, Post, path + Path.User.login)
            .asClass<HashedUser, TokenResponse>(newUserHashed("adam").asHashed())?.token

        BuiltRequest(engine, Get, path + Path.User.account, authToken)
            .asObject<User>().copy(uuid = null, dateCreated = -1, dateUpdated = -1) shouldBe newUserHashed("adam").copy(
            password = null,
            dateCreated = -1,
            dateUpdated = -1
        )
    }
}, false)
