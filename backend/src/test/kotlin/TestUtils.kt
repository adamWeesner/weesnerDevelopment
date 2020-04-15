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
fun TestApplicationEngine.request(type: HttpMethod, path: String, extraUri: String? = null) =
    handleRequest(type, "/$path${if (extraUri == null) "" else "/$extraUri"}")

/**
 * Helper method for building a test request for the given [type] and [path] having a [body].
 */
fun TestApplicationEngine.bodyRequest(type: HttpMethod, path: String, body: String?) =
    handleRequest(type, "/$path") {
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        addHeader(
            HttpHeaders.Authorization,
            "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJBdXRoZW50aWNhdGlvbiIsImF1ZCI6IndlZXNuZXJkZXZlbG9wbWVudCIsImlzcyI6IndlZXNuZXJEZXZlbG9wbWVudC5jb20iLCJhdHRyLXVzZXJuYW1lIjoiXHUwMDE2Ne-_ve-_ve-_vVx1MDAwRTVcXEzvv71rV--_vVPHuk5cdTAwMEYw77-9dU9ZXHUwMDFCau-_ve-_ve-_vXHvv71cdTAwMDbvv70iLCJhdHRyLXBhc3N3b3JkIjoi77-977-9I--_vSHvv73vv73vv73vv73vv71XXG7vv71cdTAwMUTvv71QXHUwMDAy77-9aj0yUFx1MDAxMVx1MDAxRS1M77-9acKrVO-_vSIsImV4cCI6MTU4NDYxNTgxMSwiaWF0IjoxNTg0NTc5ODExfQ.zj5cvtyYzkrJXWU56TCdcT-qRQ-A83IG7PzJ3XNBFuA"
        )
        if (body != null)
            setBody(body)
    }

/**
 * Helper method for returning the given [request] to the given [T] type from json.
 */
inline fun <reified T> TestApplicationEngine.requestToObject(type: HttpMethod, path: String) =
    request(type, path).response.content?.fromJson<T>()

/**
 * Helper method for returning the given [bodyRequest] to the given [T] type from json.
 */
inline fun <reified T> TestApplicationEngine.requestToObject(type: HttpMethod, path: String, body: String?) =
    bodyRequest(type, path, body).response.content?.fromJson<T>()