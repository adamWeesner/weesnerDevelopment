import auth.InvalidUserException
import auth.InvalidUserReason
import auth.SymmetricEncryption
import auth.TokenResponse
import com.weesnerdevelopment.Path
import io.kotlintest.shouldBe
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpStatusCode
import shared.auth.User
import shared.fromJson
import shared.toJson
import java.util.*

class UserTests : BaseTest({
    val path = Path.User.base

    val newItemNotEncrypted = User(
        name = "Adam",
        email = "randomemail@email.com",
        username = "adam.weesner",
        password = "password"
    )

    val newItemEncrypted = newItemNotEncrypted.copy(
        username = Base64.getEncoder().encodeToString(newItemNotEncrypted.username?.toByteArray()),
        password = Base64.getEncoder().encodeToString(newItemNotEncrypted.password?.toByteArray())
    )

    "access account without login gives InvalidJwt" {
        with(engine) {
            val request = request(Get, path, Path.User.account)

            val response = request.response.content?.fromJson<InvalidUserException>()
            response?.reasonCode shouldBe InvalidUserReason.InvalidJwt.code
        }
    }

    "login with non-encrypted data gives InvalidUserInfo" {
        with(engine) {
            val request = bodyRequest(Post, path + Path.User.login, newItemNotEncrypted.asHashed().toJson())

            val response = request.response.content?.fromJson<InvalidUserException>()
            response?.reasonCode shouldBe InvalidUserReason.InvalidUserInfo.code
        }
    }

    "sign up with non-encrypted data gives InvalidUserInfo" {
        with(engine) {
            val request = bodyRequest(Post, path + Path.User.signUp, newItemNotEncrypted.toJson())

            val response = request.response.content?.fromJson<InvalidUserException>()
            response?.reasonCode shouldBe InvalidUserReason.InvalidUserInfo.code
        }
    }

    "login with user data that does not exist gives NoUserFound" {
        with(engine) {
            val request = bodyRequest(Post, path + Path.User.login, newItemEncrypted.asHashed().toJson())

            val response = request.response.content?.fromJson<InvalidUserException>()
            response?.reasonCode shouldBe InvalidUserReason.NoUserFound.code
        }
    }

    "sign up gives Created" {
        with(engine) {
            val request = bodyRequest(Post, path + Path.User.signUp, newItemEncrypted.toJson())

            request.response.status() shouldBe HttpStatusCode.Created
        }
    }

    "login with created user gives OK" {
        with(engine) {
            val request = bodyRequest(Post, path + Path.User.login, newItemEncrypted.asHashed().toJson())

            request.response.status() shouldBe HttpStatusCode.OK
        }
    }

    "get account info with created user gives user data" {
        with(engine) {
            val loginRequest = bodyRequest(Post, path + Path.User.login, newItemEncrypted.asHashed().toJson())
            val authToken = loginRequest.response.content?.fromJson<TokenResponse>()

            val request = request(Get, path, Path.User.account, authToken = authToken?.token)
            val response =
                request.response.content?.fromJson<User>()?.copy(uuid = null, dateCreated = -1, dateUpdated = -1)

            response shouldBe newItemEncrypted.copy(password = null, dateCreated = -1, dateUpdated = -1)
        }
    }

    "encrypting/decrypting data works properly" {
        val secret = "this is a secret"
        val encryption = SymmetricEncryption()

        val encryptedUser = encryption.encrypt(newItemNotEncrypted.username ?: "", secret)
        println("user data $encryptedUser")
        val decryptedUser = encryption.decrypt(encryptedUser, secret)
        println("decrypted $decryptedUser")
        decryptedUser shouldBe newItemNotEncrypted.username
    }
})