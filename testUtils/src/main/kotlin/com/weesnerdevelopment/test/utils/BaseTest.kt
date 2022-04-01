package com.weesnerdevelopment.test.utils

import com.typesafe.config.ConfigFactory
import com.weesnerdevelopment.shared.Paths
import com.weesnerdevelopment.shared.auth.HashedUser
import com.weesnerdevelopment.shared.auth.TokenResponse
import com.weesnerdevelopment.shared.auth.User
import io.ktor.config.*
import io.ktor.http.HttpMethod.Companion.Delete
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpMethod.Companion.Put
import io.ktor.server.testing.*
import kimchi.Kimchi
import org.junit.jupiter.api.*
import java.io.File

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class BaseTest(configPath: String) {
    val engine = TestApplicationEngine(createTestEnvironment {
        config = HoconApplicationConfig(ConfigFactory.load(configPath))
    })

    var token: String = ""
    lateinit var signedInUser: User

    private fun deleteDb() {
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
        token = post(Paths.User.basePath + Paths.User.signUp, usingToken = null).asClass<User, TokenResponse>(
            User(
                name = "test",
                email = "test@email.com",
                username = "test",
                password = "test"
            )
        )?.token ?: post(Paths.User.basePath + Paths.User.login, usingToken = null)
            .asClass<HashedUser, TokenResponse>(HashedUser("test", "test"))?.token
                ?: throw IllegalArgumentException("Something happened... should have gotten a token")


        signedInUser = get(Paths.User.basePath + Paths.User.account).asObject()
    }

    private fun String.buildUrl(id: Int?, additional: String? = null): String {
        val addId = if (id == null) "" else "?id=$id"
        val addAdditional = additional ?: ""

        return "$this$addId$addAdditional"
    }

    fun get(url: String, id: Int? = null, usingToken: String? = token) =
        BuiltRequest(engine, Get, url.buildUrl(id), usingToken)

    fun post(url: String, id: Int? = null, usingToken: String? = token) =
        BuiltRequest(engine, Post, url.buildUrl(id), usingToken)

    fun put(url: String, id: Int? = null, additional: String? = null, usingToken: String? = token) =
        BuiltRequest(engine, Put, url.buildUrl(id, additional), usingToken)

    fun delete(url: String, id: Int? = null, usingToken: String? = token) =
        BuiltRequest(engine, Delete, url.buildUrl(id), usingToken)

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
