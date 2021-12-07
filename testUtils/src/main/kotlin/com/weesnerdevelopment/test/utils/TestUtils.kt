package com.weesnerdevelopment.test.utils

import com.typesafe.config.ConfigFactory
import com.weesnerdevelopment.shared.base.Response
import com.weesnerdevelopment.shared.base.ServerError
import com.weesnerdevelopment.shared.toJson
import io.ktor.config.*
import io.ktor.http.*
import io.ktor.server.testing.*
import parse
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Build a request to be sent to the backend.
 *
 * @param engine The test engine to be used to make the requests.
 * @param method The [HttpMethod] to send the network request with.
 * @param path The path for the request to be sent to.
 * @param token The token to be sent with the request
 */
class BuiltRequest(
    val engine: TestApplicationEngine,
    val method: HttpMethod,
    val path: String,
    val token: String? = null
) {
    /**
     * Sends the [BuiltRequest] with an optional [body].
     */
    inline fun <reified T> send(body: T? = null) = engine.handleRequest(method, path) {
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        token?.let { addHeader(HttpHeaders.Authorization, "Bearer $it") }
        body?.toJson()?.let { setBody(it) }
    }

    /**
     * [send] the request returning the response as [T].
     */
    inline fun <reified T> asObject(body: T? = null) =
        send(body).response.content.parse<Response>().message.let {
            when (it) {
                is String -> it.parse<T>()
                else -> it.toJson().parse<T>()
            }
        }

    /**
     * [send] the request returning the response as [T].
     */
    inline fun <reified T, reified R> asServerError(body: T? = null) =
        send(body).response.content.parse<ServerError>().message.parse<R>()

    /**
     * [send] the request returning the response as [T].
     */
    inline fun <reified T, reified R> asClass(body: T? = null) =
        send(body).response.content.parseResponse<R>()

    /**
     * [send] the request returning the status of the response.
     */
    inline fun <reified T> sendStatus(body: T? = null) = send(body).response.status()
}

inline fun <reified T> String?.parseResponse() = this.parse<Response>().let {
    if (it.status.code.toString().startsWith("4")) null
    else {
        when (val message = it.message) {
            is String -> message.parse<T>()
            else -> message.toJson().parse<T>()
        }
    }
}

infix fun <A> A.shouldBe(expected: A) = assertEquals(expected, this)
infix fun <A> A.shouldNotBe(expected: A) = assertNotEquals(expected, this)
infix fun Int.shouldBeAtLeast(expected: Int) = assert(this >= expected)
infix fun Int.shouldBeAtMost(expected: Int) = assert(this <= expected)

/**
 * Wraps [withTestApplication] with our [Application::module] to make things simpler to use
 */
fun testApp(configPath: String, test: TestApplicationEngine.() -> Unit) {
    val engine = TestApplicationEngine(createTestEnvironment {
        config = HoconApplicationConfig(ConfigFactory.load(configPath))
    })
    engine.start()
    test(engine)
    engine.environment.stop()
}

/**
 * Reads the [fileName].json from test/resources, useful for passing in json objects for tests :)
 */
fun fromFile(fileName: String): String =
    File("src/test/resources/${fileName}.json").readLines().joinToString("\n")

/**
 * Calls [fromFile] with [baseUrl]/[fileName] for easier use in some places
 */
fun fromFile(baseUrl: String, fileName: String) = fromFile("$baseUrl/$fileName")

/**
 * Adds the `Content-Type: application/json` header for a request that has a body coming from [bodyFileName]
 */
fun TestApplicationEngine.handleRequest(
    method: HttpMethod,
    uri: String,
    bodyFileName: String? = null,
    bearerToken: String? = null
) = handleRequest(method, uri) {
    addHeader("Content-Type", "application/json")
    if (bearerToken != null)
        addHeader("Authorization", "Bearer $bearerToken")

    bodyFileName?.let {
        setBody(fromFile(uri, it))
    }
}
