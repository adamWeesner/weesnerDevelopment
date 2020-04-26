import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.*
import shared.fromJson

/**
 * Helper method for building a test request for the given [type] and [path] having [extraUri] will add a /[extraUri]
 * to the end of the url to do the request type on.
 */
fun TestApplicationEngine.request(
    type: HttpMethod,
    path: String,
    extraUri: String? = null,
    authToken: String? = null,
    additionalRequestInfo: (request: TestApplicationRequest) -> Unit = {}
): TestApplicationCall {
    val basePath = if (!path.startsWith("/")) "/$path" else path

    val extras = if (extraUri != null) {
        if (extraUri.startsWith("/")) extraUri
        else "/$extraUri"
    } else {
        ""
    }
    return handleRequest(type, "$basePath$extras") {
        addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        if (!authToken.isNullOrBlank())
            addHeader(HttpHeaders.Authorization, "Bearer $authToken")
        additionalRequestInfo(this)
    }
}

/**
 * Helper method for building a test request for the given [type] and [path] having a [body].
 */
fun TestApplicationEngine.bodyRequest(type: HttpMethod, path: String, body: String?, authToken: String? = null) =
    request(type = type, path = path, authToken = authToken, additionalRequestInfo = {
        if (body != null) it.setBody(body)
    })

/**
 * Helper method for returning the given [request] to the given [T] type from json.
 */
inline fun <reified T> TestApplicationEngine.requestToObject(
    type: HttpMethod,
    path: String,
    authToken: String? = null
) =
    request(type, path, authToken = authToken).response.content?.fromJson<T>()

/**
 * Helper method for returning the given [bodyRequest] to the given [T] type from json.
 */
inline fun <reified T> TestApplicationEngine.requestToObject(
    type: HttpMethod,
    path: String,
    body: String?,
    authToken: String? = null
) =
    bodyRequest(type, path, body, authToken).response.content?.fromJson<T>()
