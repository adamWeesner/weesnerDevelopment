import auth.CustomPrincipal
import auth.InvalidUserException
import auth.InvalidUserReason
import io.ktor.application.ApplicationCall
import io.ktor.auth.authentication
import io.ktor.http.HttpStatusCode
import io.ktor.request.uri
import io.ktor.response.respond
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import shared.toJson

/**
 * Helper function to query [T] in the table.
 */
suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction { block() }

/**
 * Server Error generating a nice looking json error when there is a server issue.
 */
data class ServerError(
    val status: String,
    val statusCode: Int,
    val message: String
)

/**
 * Helper function to [respond] with a [ServerError].
 */
suspend fun ApplicationCall.respondServerError(error: Throwable) {
    val httpStatus = HttpStatusCode.InternalServerError
    respond(
        httpStatus,
        ServerError(
            httpStatus.description,
            httpStatus.value,
            error.localizedMessage ?: error.message ?: error.toString()
        )
    )
}

/**
 * Helper function to [respond] with an [InvalidUserException] with the given [reason].
 */
suspend fun ApplicationCall.respondAuthorizationIssue(reason: InvalidUserReason) {
    val httpStatus = HttpStatusCode.Unauthorized
    respond(httpStatus, InvalidUserException(request.uri, httpStatus.value, reason.code).toJson() ?: "")
}

/**
 * Gets the Authentication principal from the [ApplicationCall].
 */
fun ApplicationCall.loggedUserData() = authentication.principal<CustomPrincipal>()
