import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import shared.base.Response
import shared.base.ServerError
import shared.toJson

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
        send<T>(body).response.content.parse<Response>().message.let {
            if (it is String) it.parse<T>()
            else it.toJson().parse<T>()
        }

    /**
     * [send] the request returning the response as [T].
     */
    inline fun <reified T, reified R> asServerError(body: T? = null) =
        send<T>(body).response.content.parse<ServerError>().message.toJson().parse<R>()

    /**
     * [send] the request returning the response as [T].
     */
    inline fun <reified T, reified R> asClass(body: T? = null) =
        send<T>(body).response.content.also { println("content $it") }.parseResponse<R>()

    /**
     * [send] the request returning the status of the response.
     */
    inline fun <reified T> sendStatus(body: T? = null) = send<T>(body).response.status()
}

inline fun <reified T> String?.parseResponse() = this.parse<Response>().let {
    if (it.status.code.toString().startsWith("4")) null
    else it.message.toJson().parse<T>()
}
