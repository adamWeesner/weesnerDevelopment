import auth.InvalidUserException
import auth.InvalidUserReason
import auth.TokenResponse
import com.weesnerdevelopment.utils.Path
import io.kotlintest.shouldBe
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpStatusCode
import shared.auth.HashedUser
import shared.auth.User
import shared.fromJson
import java.util.*

class UserTests : BaseTest({
    val newUser = User(
        name = "Adam",
        email = "randomemail@email.com",
        username = "adam.weesner",
        password = "password"
    )

    val newUserHashed = newUser.copy(
        username = Base64.getEncoder().encodeToString(newUser.username?.toByteArray()),
        password = Base64.getEncoder().encodeToString(newUser.password?.toByteArray())
    )

    val path = Path.User.base

    "access account without login gives InvalidJwt" {
        BuiltRequest(
            engine,
            Get,
            path + Path.User.account
        ).asObject<InvalidUserException>()?.reasonCode shouldBe InvalidUserReason.InvalidJwt.code
    }

    "login with non-encrypted data gives InvalidUserInfo" {
        BuiltRequest(
            engine,
            Post,
            path + Path.User.login
        ).asClass<HashedUser, InvalidUserException>(newUser.asHashed())?.reasonCode shouldBe InvalidUserReason.InvalidUserInfo.code
    }

    "sign up with non-encrypted data gives InvalidUserInfo" {
        BuiltRequest(
            engine,
            Post,
            path + Path.User.signUp
        ).asClass<User, InvalidUserException>(newUser)?.reasonCode shouldBe InvalidUserReason.InvalidUserInfo.code
    }

    "login with user data that does not exist gives NoUserFound" {
        BuiltRequest(
            engine,
            Post,
            path + Path.User.login
        ).asClass<HashedUser, InvalidUserException>(newUserHashed.asHashed())?.reasonCode shouldBe InvalidUserReason.NoUserFound.code
    }

    "sign up gives Created" {
        BuiltRequest(engine, Post, path + Path.User.signUp).sendStatus(newUserHashed) shouldBe HttpStatusCode.Created
    }

    "login with created user gives OK" {
        BuiltRequest(
            engine,
            Post,
            path + Path.User.login
        ).sendStatus(newUserHashed.asHashed()) shouldBe HttpStatusCode.OK
    }

    "get account info with created user gives user data" {
        val loginRequest = BuiltRequest(engine, Post, path + Path.User.login).send(newUserHashed.asHashed())
        val authToken = loginRequest.response.content?.fromJson<TokenResponse>()

        BuiltRequest(engine, Get, path + Path.User.account, authToken?.token).asObject<User>()
            ?.copy(uuid = null, dateCreated = -1, dateUpdated = -1) shouldBe newUserHashed.copy(
            password = null,
            dateCreated = -1,
            dateUpdated = -1
        )
    }
})
