package com.weesnerdevelopment.auth

import com.weesnerdevelopment.businessRules.parse
import com.weesnerdevelopment.shared.Paths
import com.weesnerdevelopment.shared.auth.TokenUser
import com.weesnerdevelopment.shared.auth.User
import com.weesnerdevelopment.test.utils.fromFile
import com.weesnerdevelopment.test.utils.handleRequest
import com.weesnerdevelopment.test.utils.shouldBe
import com.weesnerdevelopment.test.utils.testApp
import io.ktor.http.*
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Post
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TestUserRoutes : AuthTests() {
    override val baseUrl = Paths.User.basePath
    private val testUser = "testing@gmail.com"
    private val testPass = "randompassword"

    @Nested
    @DisplayName("sign up")
    inner class SignUp : Testing() {
        @Test
        fun `sign up`() = testApp(config) { _ ->
            val call = handleRequest(Post, Paths.User.basePath, "signUpEncryptedRequest")
            call.response.status() shouldBe HttpStatusCode.Created
        }
    }

    @Nested
    @DisplayName("login")
    inner class Login : Testing() {
        @Test
        fun `access account without login`() = testApp(config) { _ ->
            val call = handleRequest(Get, Paths.User.account)
            call.response.content shouldBe fromFile(baseUrl, "get/noTokenResponse")
        }

        @Test
        fun `login with user data that does not exist`() = testApp(config) { _ ->
            val call = handleRequest(Get, "${Paths.User.basePath}?email=testing3@gmail.com&password=$testPass")
            call.response.content shouldBe fromFile(baseUrl, "login/noUserFoundResponse")
        }

        @Test
        fun login() = testApp(config) { _ ->
            handleRequest(Post, Paths.User.basePath, "signUpEncryptedRequest")

            val call = handleRequest(Get, "${Paths.User.basePath}?email=$testUser&password=$testPass")
            call.response.status() shouldBe HttpStatusCode.OK
        }

        @Test
        fun `get account info with created user`() = testApp(config) { _ ->
            handleRequest(Post, Paths.User.basePath, "signUpEncryptedRequest")

            val login = handleRequest(Get, "${Paths.User.basePath}?email=$testUser&password=$testPass")
            val authToken = login.response.content.parse<TokenUser>().authToken

            val call = handleRequest(Get, Paths.User.account, bearerToken = authToken)
            call.response.status() shouldBe HttpStatusCode.OK
            call.response.content.parse<User>().apply {
                email shouldBe "testing@gmail.com"
            }
        }
    }
}
