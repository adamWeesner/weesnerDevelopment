import auth.CustomPrincipal
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import kimchi.Kimchi
import kotlinx.io.core.toByteArray
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import shared.auth.InvalidUserException
import shared.auth.InvalidUserReason
import shared.base.GenericItem
import shared.base.Response
import shared.base.Response.Companion.InternalError
import shared.base.Response.Companion.Unauthorized
import shared.base.ServerError
import shared.fromJson

/**
 * Helper function to query [T] in the table.
 */
suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction { block() }

data class CallItem(
    val instance: String,
    val time: Long = System.currentTimeMillis()
)

data class HttpLog(
    val method: String,
    val url: String,
    val statusCode: Int
) {
    constructor(method: HttpMethod, url: String, statusCode: HttpStatusCode) : this(method.value, url, statusCode.value)
}

val callItems = mutableListOf<CallItem>()

/**
 * Helper function to log request and body.
 */
fun <I : GenericItem> PipelineContext<*, ApplicationCall>.logRequest(body: I? = null) {
    callItems.add(CallItem(this.toString()))
    val request = call.request.origin
    val url = "${request.scheme}://${request.remoteHost}:${request.port}/${request.uri}"
    val method = call.request.httpMethod.value

    Kimchi.debug("--> $method ${request.version} $url")
    if (!call.request.authorization().isNullOrBlank())
        Kimchi.debug("authorization: ${call.request.authorization()}")
    if (body != null)
        Kimchi.debug("body: $body")
    Kimchi.debug("--> END $method")
}

/**
 * Helper function to [respond] with a [Response] and body.
 */
suspend fun PipelineContext<*, ApplicationCall>.respond(response: Response) = response.run {
    if (!call.request.origin.uri.contains(Path.BillMan.logging))
        Kimchi.info("${HttpLog(call.request.httpMethod.value, call.request.origin.uri, status.code)}")

    call.respond(HttpStatusCode(status.code, status.description), this).also {
        val callItem = callItems.firstOrNull { it.instance == this@respond.toString() }
        callItems.remove(callItem)

        val time = System.currentTimeMillis() - (callItem?.time ?: System.currentTimeMillis())

        Kimchi.debug("<-- ${call.request.origin.version} (${time}ms)")
        Kimchi.debug("Response: $message")
        Kimchi.debug("<-- END HTTP (${response.message.toString().toByteArray().size}-byte body)")
    }
}

/**
 * Helper function to [respond] with a [Response] and error body.
 */
suspend fun PipelineContext<*, ApplicationCall>.respondError(error: Response) = error.run {
    if (!call.request.origin.uri.contains(Path.BillMan.logging))
        Kimchi.info("${HttpLog(call.request.httpMethod.value, call.request.origin.uri, status.code)}")

    call.respond(
        HttpStatusCode(status.code, status.description),
        ServerError(status.description, status.code, message)
    ).also {
        val callItem = callItems.firstOrNull { it.instance == this@respondError.toString() }
        callItems.remove(callItem)

        val time = System.currentTimeMillis() - (callItem?.time ?: System.currentTimeMillis())

        Kimchi.debug("<-- ${call.request.origin.version} (${time}ms)")
        Kimchi.debug("Response: $message")
        Kimchi.debug("<-- END HTTP (${error.message.toString().toByteArray().size}-byte body)")
    }
}

/**
 * Helper function to [respond] with a [ServerError].
 */
suspend fun PipelineContext<*, ApplicationCall>.respondErrorServer(error: Throwable) =
    respondError(InternalError(error.message ?: error.toString()))

/**
 * Helper function to [respond] with an [InvalidUserException] with the given [reason].
 */
suspend fun PipelineContext<*, ApplicationCall>.respondErrorAuthorizing(reason: InvalidUserReason) =
    respondError(Unauthorized(InvalidUserException(call.request.uri, HttpStatusCode.Unauthorized.value, reason.code)))

/**
 * Gets the Authentication principal from the [ApplicationCall].
 */
fun ApplicationCall.loggedUserData() = authentication.principal<CustomPrincipal>()

inline fun <reified T> String?.parse(): T =
    this?.fromJson<T>() ?: throw Throwable("failed to parse $this to ${T::class}.")

/**
 * Checks whether the Int is a valid successful id, added to the database.
 */
val Int?.isNotValidId get() = this == null || this == -1

/**
 * Helper function to be able to do something like `service.get { id eq item.id }`
 * instead of `service.get { service.table.id eq item.id }`.
 */
suspend inline fun <reified O, reified T, reified R : BaseService<O, T>> R.get(crossinline query: O.(SqlExpressionBuilder) -> Op<Boolean>) {
    this.get { this@get.table.query(this) }
}
