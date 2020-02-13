import com.weesnerdevelopment.Paths
import com.weesnerdevelopment.fromJson
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody

/**
 * Helper method for building a test request for the given [type] and [path] having [extraUri] will add a /[extraUri]
 * to the end of the url to do the request type on.
 */
fun TestApplicationEngine.request(type: HttpMethod, path: Paths, extraUri: String? = null) =
    handleRequest(type, "/${path.name}${if (extraUri == null) "" else "/$extraUri"}")

/**
 * Helper method for building a test request for the given [type] and [path] having a [body].
 */
fun TestApplicationEngine.bodyRequest(type: HttpMethod, path: Paths, body: String) =
    handleRequest(type, "/${path.name}") {
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(body)
    }

/**
 * Helper method for returning the given [request] to the given [T] type from json.
 */
inline fun <reified T> TestApplicationEngine.requestToObject(type: HttpMethod, path: Paths) =
    request(type, path).response.content?.fromJson<T>()

/**
 * Helper method for returning the given [bodyRequest] to the given [T] type from json.
 */
inline fun <reified T> TestApplicationEngine.requestToObject(type: HttpMethod, path: Paths, body: String) =
    bodyRequest(type, path, body).response.content?.fromJson<T>()