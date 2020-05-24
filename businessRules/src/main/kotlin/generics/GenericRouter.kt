package generics

import auth.InvalidUserReason
import auth.UsersService
import diff
import history.HistoryService
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.DefaultWebSocketSession
import io.ktor.http.cio.websocket.Frame
import io.ktor.request.receive
import io.ktor.routing.*
import io.ktor.util.pipeline.PipelineContext
import loggedUserData
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import respond
import respondErrorAuthorizing
import shared.auth.HashedUser
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
                call.respond(Ok(response))
            } ?: call.respond(NotFound("Could not get items."))
        }
    }

    /**
     * Get a single item with the given {pathParam} if there is one or [HttpStatusCode.NotFound].
     *
     * GET /basePath/{pathParam}
     */
    open fun Route.getSingle() {
        get("/{item}") {
            val param = call.parameters["item"] ?: return@get call.respond(BadRequest("Invalid param."))
            when (val retrieved = service.getSingle { singleEq(param) }) {
                null -> call.respond(NotFound("Could not get item for param $param."))
                else -> call.respond(Ok(retrieved))
            }
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

            if (postQualifier(item) != null) return@post call.respond(Conflict("Item matching $item already in db."))

            when (val added = service.add(item)) {
                null -> call.respond(Conflict("An error occurred add item $item."))
                else -> call.respond(Created(added))
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
        val user = tokenAsUser(usersService)

        if (user == null) {
            call.respondErrorAuthorizing(InvalidUserReason.NoUserFound)
            return null
        }

        val history = mutableListOf<History>()
        item.diff(updatedItem, user).forEach {
            historyService.add(it)?.let(history::add)
        }

        return history
    }

    suspend fun PipelineContext<Unit, ApplicationCall>.tokenAsUser(usersService: UsersService) =
        call.loggedUserData()?.getData()?.let {
            if (it.username == null || it.password == null) {
                call.respondErrorAuthorizing(InvalidUserReason.NoUserFound)
                return null
            } else {
                usersService.getUserFromHash(HashedUser(it.username, it.password))
            }
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
                updated = putAdditional(oldItem, item)

            when {
                updated == null -> call.respond(BadRequest("An error occurred updating $item."))
                updated.id != item.id -> call.respond(Created(updated))
                else -> call.respond(Ok(updated))
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
            val param = call.parameters["item"] ?: return@delete call.respond(BadRequest("Invalid param."))

            val id = deleteQualifier(param)?.id
                ?: return@delete call.respond(NotFound("Item matching $param was not found."))

            val removed = service.delete(id) { singleEq(param) }

            call.respond(if (removed) Ok("Successfully removed item.") else NotFound("Item matching $param was not found."))
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
