import auth.CustomPrincipal
import auth.InvalidUserException
import auth.InvalidUserReason
import generics.InternalError
import generics.Response
import generics.Unauthorized
import io.ktor.application.ApplicationCall
import io.ktor.auth.authentication
import io.ktor.http.HttpStatusCode
import io.ktor.request.uri
import io.ktor.response.respond
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import shared.auth.User
import shared.base.GenericItem
import shared.base.History
import shared.base.HistoryItem
import shared.fromJson
import kotlin.reflect.full.declaredMemberProperties

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
    val message: Any
)

/**
 * Helper function to [respond] with a [Response] and body.
 */
suspend fun ApplicationCall.respond(response: Response) = response.run { respond(status, message) }

/**
 * Helper function to [respond] with a [Response] and error body.
 */
suspend fun ApplicationCall.respondError(error: Response) =
    error.run { respond(status, ServerError(status.description, status.value, message)) }

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
    Categories
}

/**
 * Splits the [GenericItem] generic item in to a list of its parameters names and their values.
 */
fun GenericItem.split(): List<Pair<String, Any?>> {
    val split = mutableListOf<Pair<String, Any?>>()
    this.javaClass.kotlin.declaredMemberProperties.forEach {
        if (
            it.name == HistoryItem::history.name
            || it.name == GenericItem::dateCreated.name
            || it.name == GenericItem::dateUpdated.name
        ) return@forEach

        val item = it.get(this)

        if (item is List<*>) item.filterIsInstance<GenericItem>().forEach { listItem -> split += listItem.split() }
        else split.add(Pair("${this::class.simpleName} $id ${it.name}", item))
    }

    return split.toList().sortedBy { it.first }
}

/**
 * Diff's two [O] items, for the [user].
 *
 * @return list of [History], essentially a list of the differences between the first and second
 */
inline fun <reified O : GenericItem> O.diff(other: O, user: User): List<History> {
    val secondItem = other.split()

    return this.split().mapIndexedNotNull { index: Int, item: Pair<String, Any?> ->
        val otherItem = secondItem[index]
        if (item.second != otherItem.second)
            History(field = item.first, oldValue = item.second, newValue = otherItem.second, updatedBy = user)
        else null
    }.toList()
}

inline fun <reified T> String?.parse(): T =
    this?.fromJson<T>() ?: throw Throwable("failed to parse $this to ${T::class}.")
