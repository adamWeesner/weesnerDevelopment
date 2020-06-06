import auth.CustomPrincipal
import io.ktor.application.ApplicationCall
import io.ktor.auth.authentication
import io.ktor.http.HttpStatusCode
import io.ktor.request.uri
import io.ktor.response.respond
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import shared.auth.InvalidUserException
import shared.auth.InvalidUserReason
import shared.base.InternalError
import shared.base.Response
import shared.base.ServerError
import shared.base.Unauthorized
import shared.fromJson

/**
 * Helper function to query [T] in the table.
 */
suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction { block() }

/**
 * Helper function to [respond] with a [Response] and body.
 */
suspend fun ApplicationCall.respond(response: Response) =
    response.run { respond(HttpStatusCode(status.code, status.description), message) }

/**
 * Helper function to [respond] with a [Response] and error body.
 */
suspend fun ApplicationCall.respondError(error: Response) =
    error.run {
        respond(
            HttpStatusCode(status.code, status.description),
            ServerError(status.description, status.code, message)
        )
    }

/**
 * Helper function to [respond] with a [ServerError].
 */
suspend fun ApplicationCall.respondErrorServer(error: Throwable) =
    respondError(InternalError(error.message ?: error.toString()))

/**
 * Helper function to [respond] with an [InvalidUserException] with the given [reason].
 */
suspend fun ApplicationCall.respondErrorAuthorizing(reason: InvalidUserReason) =
    respondError(Unauthorized(InvalidUserException(request.uri, HttpStatusCode.Unauthorized.value, reason.code)))

/**
 * Gets the Authentication principal from the [ApplicationCall].
 */
fun ApplicationCall.loggedUserData() = authentication.principal<CustomPrincipal>()

enum class HistoryTypes {
    Bill,
    Color,
    Categories,
    Occurrence,
    Payment
}

inline fun <reified T> String?.parse(): T =
    this?.fromJson<T>() ?: throw Throwable("failed to parse $this to ${T::class}.")
