package generics

import auth.UsersService
import diff
import history.HistoryService
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.DefaultWebSocketSession
import io.ktor.http.cio.websocket.Frame
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.util.pipeline.PipelineContext
import loggedUserData
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import shared.auth.HashedUser
import shared.auth.User
import shared.base.GenericItem
import shared.base.History
import shared.base.HistoryItem
import kotlin.reflect.KType

/**
 * Router for the given item type [O] in the table [T]
 *
 * [getDefault]
 *
 * [getSingle]
 *
 * [postDefault]
 *
 * [putDefault]
 *
 * [deleteDefault]
 *
 * [webSocketDefault]
 */
abstract class GenericRouter<O : GenericItem, T : IdTable>(
    val basePath: String,
    val service: GenericService<O, T>,
    private val response: GenericResponse<O>? = null
) {
    lateinit var itemType: KType

    /**
     * The equation to use to identify which item to do an action on.
     */
    open fun singleEq(param: String) = service.table.id eq param.toInt()

    /**
     * The qualifier to verify if the [postDefault] item is already in the database or not.
     */
    open suspend fun postQualifier(receivedItem: O): O? =
        service.getSingle { service.table.id eq (receivedItem.id ?: -1) }

    /**
     * The qualifier to verify if the [deleteDefault] item is in the database before trying to delete it.
     */
    open suspend fun deleteQualifier(param: String): O? = service.getSingle { singleEq(param) }

    /**
     * The qualifier to verify if the the [putDefault] item is in the database before trying to update it.
     */
    open suspend fun putQualifier(receivedItem: O): O? =
        service.update(receivedItem) { service.table.id eq receivedItem.id!! }

    /**
     * Get all items in [T] if there are any or returns an [HttpStatusCode.NotFound]
     *
     * GET /basePath
     */
    open fun Route.getDefault() {
        get("/") {
            response?.let {
                it.items = service.getAll()
                call.respond(response)
            } ?: call.respond(HttpStatusCode.NotFound)
        }
    }

    /**
     * Get a single item with the given {pathParam} if there is one or [HttpStatusCode.NotFound].
     *
     * GET /basePath/{pathParam}
     */
    open fun Route.getSingle() {
        get("/{item}") {
            val param = call.parameters["item"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            call.respond(service.getSingle { singleEq(param) } ?: HttpStatusCode.NotFound)
        }
    }

    /**
     * Attempts to add the item in the body of the http request, if there is already an item in the table that matches
     * responds with [HttpStatusCode.Conflict]
     *
     * The JSON body in the POST request can have all of the non-nullable values for the item, generally `id`,
     * `dateCreated`, and `dateUpdated` be omitted if you choose, but all other item values should be provided
     * example for [SocialSecurity]:
     *
     * POST /basePath
     * ```json
     * {
     *   "year": 2016,
     *   "percent": 1.45,
     *   "limit": 125000
     * }
     * ```
     */
    open fun Route.postDefault() {
        post("/") {
            val item = call.receive<O>(itemType)

            if (postQualifier(item) != null) return@post call.respond(HttpStatusCode.Conflict)

            when (val added = service.add(item)) {
                null -> call.respond(HttpStatusCode.Conflict)
                else -> call.respond(HttpStatusCode.Created, added)
            }
        }
    }

    /**
     * Attempts to update additional information that pertains to the [item], things like a [HistoryItem] or
     * other things. By default this function does nothing.
     */
    open suspend fun PipelineContext<Unit, ApplicationCall>.putAdditional(item: O, updatedItem: O): O? = updatedItem

    suspend inline fun <reified O : GenericItem> PipelineContext<Unit, ApplicationCall>.handleHistory(
        item: O,
        updatedItem: O,
        usersService: UsersService,
        historyService: HistoryService
    ): List<History>? {
        val callData = call.loggedUserData()?.getData()
        val user: User? = callData?.let {
            if (it.username == null || it.password == null) {
                call.respond(HttpStatusCode.Unauthorized)
                return null
            }
            usersService.getUserFromHash(HashedUser(it.username, it.password))
        }

        if (user == null) {
            call.respond(HttpStatusCode.Unauthorized)
            return null
        }

        val history = mutableListOf<History>()
        item.diff(updatedItem, user).forEach {
            historyService.add(it)?.let(history::add)
        }

        return history
    }

    /**
     * Attempts to update the item in the body of the http request. If no `id` is given in the body then the item tries
     * to add itself to the database. If something happens when updating or adding the item we respond with
     * [HttpStatusCode.BadRequest]. Pass in the `id` along with all other non-nullable fields for the item, in the
     * JSON request to determine which record to update.
     *
     * PUT /
     */
    open fun Route.putDefault() {
        put("/") {
            val item = call.receive<O>(itemType)
            val oldItem = item.id?.let { service.getSingle { service.table.id eq it } }

            var updated = putQualifier(item)

            if (oldItem != null && updated != null)
                updated = putAdditional(oldItem, updated)

            when {
                updated == null -> call.respond(HttpStatusCode.BadRequest)
                updated.id != item.id -> call.respond(HttpStatusCode.Created, updated)
                else -> call.respond(HttpStatusCode.OK, updated)
            }
        }
    }

    /**
     * Attempt to delete the item that matches the given {pathParam} or [HttpStatusCode.BadRequest] if a {pathParam} is
     * not given. If there is not item with the given {pathParam}, or there was an error deleting the item we respond
     * with [HttpStatusCode.NotFound].
     *
     * DELETE /basePath/{pathParam}
     */
    open fun Route.deleteDefault() {
        delete("/{item}") {
            val param = call.parameters["item"] ?: return@delete call.respond(HttpStatusCode.BadRequest)

            val id = deleteQualifier(param)?.id ?: return@delete call.respond(HttpStatusCode.NotFound)

            val removed = service.delete(id) { singleEq(param) }

            call.respond(if (removed) HttpStatusCode.OK else HttpStatusCode.NotFound)
        }
    }

    /**
     * returns [Notification] instances containing the change `type`, `id` and `entity` (if applicable) e.g:
     *
     * WS /updates
     * ```json
     * {
     *   "type": "CREATE",
     *   "id": 12,
     *   "entity": {
     *       "id": 12,
     *       "name": "widget1",
     *       "quantity": 5,
     *       "dateCreated": 1533583858169
     *       "dateUpdated": 1533583858169
     *   }
     * }
     * ```
     */
    open fun DefaultWebSocketSession.webSocketDefault() {
        try {
            service.addChangeListener(this.hashCode()) {
                outgoing.send(Frame.Text(it.toString()))
            }
        } finally {
            service.removeChangeListener(this.hashCode())
        }
    }
}
