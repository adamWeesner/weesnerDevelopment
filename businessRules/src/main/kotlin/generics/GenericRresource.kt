package generics

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.Frame
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.websocket.webSocket

/**
 * ## Routes for the given item type [O] in the table [T]:
 *
 * > GET /[basePath] --> get all items in the database
 *
 * > GET /[basePath]/{id} --> get one item instance by `id`
 *
 * > POST /[basePath] --> add a new item by providing a JSON object with all of the non-nullable values for the item,
 * > generally `id`, `dateCreated`, and `dateUpdated` can be omitted if you choose, but all other item values should be
 * > provided example for [SocialSecurity]:
 * ```json
 * {
 *   "year": 2016,
 *   "percent": 1.45,
 *   "limit": 125000
 * }
 * ```
 *
 * > PUT /[basePath] --> update an existing items values. Pass in the `id` along with all other non-nullable fields for
 * > the item, in the JSON request to determine which record to update, passing no `id` creates a new item
 *
 * > DELETE /[basePath]/{id} --> delete the item with the specified id
 *
 *
 * > WS /updates --> returns Notification instances containing the change `type`, `id` and `entity` (if applicable) e.g:
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
inline fun <reified O : GenericItem, T : IdTable> Route.route(
    basePath: String,
    service: GenericService<O, T>,
    crossinline getMessage: suspend (items: List<O>) -> Any
) {
    route("/$basePath") {
        get("/") {
            call.respond(getMessage(service.getAll()))
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalStateException("Must provide id")

            call.respond(service.getSingle(id) ?: HttpStatusCode.NotFound)
        }

        post("/") {
            val item = call.receive<O>()

            when (val added = service.add(item)) {
                null -> call.respond(HttpStatusCode.Conflict)
                else -> call.respond(HttpStatusCode.Created, added)
            }
        }

        put("/") {
            val item = call.receive<O>()
            val updated = service.update(item)

            when {
                updated == null -> call.respond(HttpStatusCode.NotFound)
                updated.id != item.id -> call.respond(HttpStatusCode.Created, updated)
                else -> call.respond(HttpStatusCode.OK, updated)
            }
        }

        delete("/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Must provide id")
            val removed = service.delete(id)

            call.respond(if (removed) HttpStatusCode.OK else HttpStatusCode.NotFound)
        }
    }

    webSocket("/updates") {
        try {
            service.addChangeListener(this.hashCode()) {
                outgoing.send(Frame.Text(it.toString()))
            }
        } finally {
            service.removeChangeListener(this.hashCode())
        }
    }
}