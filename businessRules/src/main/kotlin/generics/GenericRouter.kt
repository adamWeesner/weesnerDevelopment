package generics

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.DefaultWebSocketSession
import io.ktor.http.cio.websocket.Frame
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
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
    val service: GenericService<O, T>,
    private val response: GenericResponse<O>? = null
) {
    /**
     * The [getSingle] param name, what will be filtering the pathParam on.
     */
    open val getParamName: String = "id"

    /**
     * The [deleteDefault] param name, what will be filtering the pathParam on.
     */
    open val deleteParamName: String = "id"

    /**
     * The delete equation to use to identifty which item to delete.
     */
    open fun deleteEq(param: String) = service.table.id eq param.toInt()

    /**
     * The qualifier to verify if the [postDefault] item is already in the database or not.
     */
    open suspend fun postQualifier(receivedItem: O): O? =
        service.getSingle { service.table.id eq (receivedItem.id ?: -1) }

    /**
     * The qualifier to verify if the [postDefault] item is in the database before trying to delete it.
     */
    open suspend fun deleteQualifier(param: String): O? = service.getSingle { deleteEq(param) }

    /**
     * Get all items in [T] if there are any or returns an [HttpStatusCode.NotFound]
     *
     * GET /[basePath]
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
     * Get a single item with the given [pathParam] if there is one or [HttpStatusCode.NotFound].
     *
     * GET /[basePath]/{pathParam}
     */
    open fun Route.getSingle(pathParam: String) {
        get("/{$pathParam}") {
            val param = call.parameters[pathParam] ?: return@get call.respond(HttpStatusCode.BadRequest)

            call.respond(service.getSingle { service.table.id eq param.toInt() } ?: HttpStatusCode.NotFound)
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
     * POST /[basePath]
     * ```json
     * {
     *   "year": 2016,
     *   "percent": 1.45,
     *   "limit": 125000
     * }
     * ```
     */
    open fun Route.postDefault(type: KType) {
        post("/") {
            val item = call.receive<O>(type)

            if (postQualifier(item) != null) return@post call.respond(HttpStatusCode.Conflict)

            when (val added = service.add(item)) {
                null -> call.respond(HttpStatusCode.Conflict)
                else -> call.respond(HttpStatusCode.Created, added)
            }
        }
    }

    /**
     * Attempts to update the item in the body of the http request. If no `id` is given in the body then the item tries
     * to add itself to the database. If something happens when updating or adding the item we respond with
     * [HttpStatusCode.BadRequest]. Pass in the `id` along with all other non-nullable fields for the item, in the
     * JSON request to determine which record to update.
     *
     * PUT /[basePath]
     */
    open fun Route.putDefault(type: KType) {
        put("/") {
            val item = call.receive<O>(type)
            val updated = service.update(item) { service.table.id eq item.id!! }

            when {
                updated == null -> call.respond(HttpStatusCode.BadRequest)
                updated.id != item.id -> call.respond(HttpStatusCode.Created, updated)
                else -> call.respond(HttpStatusCode.OK, updated)
            }
        }
    }

    /**
     * Attempt to delete the item that matches the given [pathParam] or [HttpStatusCode.BadRequest] if a [pathParam] is
     * not given. If there is not item with the given [pathParam], or there was an error deleting the item we respond
     * with [HttpStatusCode.NotFound].
     *
     * DELETE /[basePath]/{pathParam}
     */
    open fun Route.deleteDefault(pathParam: String) {
        delete("/{$pathParam}") {
            val param = call.parameters[pathParam] ?: return@delete call.respond(HttpStatusCode.BadRequest)

            val id = deleteQualifier(param)?.id ?: return@delete call.respond(HttpStatusCode.NotFound)

            val removed = service.delete(id) { deleteEq(param) }

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