package generics

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.DefaultWebSocketSession
import io.ktor.http.cio.websocket.Frame
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import kotlin.reflect.KType

abstract class GenericRouter<O : GenericItem, T : IdTable>(
    val service: GenericService<O, T>,
    private val response: GenericResponse<O>? = null
) {
    open fun Route.getDefault() {
        get("/") {
            response?.let {
                it.items = service.getAll()
                call.respond(response)
            } ?: call.respond(HttpStatusCode.NotFound)
        }
    }

    open fun Route.getSingle(pathParam: String) {
        get("/{$pathParam}") {
            val param = call.parameters[pathParam] ?: return@get call.respond(HttpStatusCode.BadRequest)

            call.respond(service.getSingle { service.table.id eq param.toInt() } ?: HttpStatusCode.NotFound)
        }
    }

    open fun Route.postDefault(type: KType) {
        post("/") {
            val item = call.receive<O>(type)

            when (val added = service.add(item)) {
                null -> call.respond(HttpStatusCode.Conflict)
                else -> call.respond(HttpStatusCode.Created, added)
            }
        }
    }

    open fun Route.putDefault(type: KType) {
        put("/") {
            val item = call.receive<O>(type)
            val updated = service.update(item) { service.table.id eq item.id!! }

            when {
                updated == null -> call.respond(HttpStatusCode.NotFound)
                updated.id != item.id -> call.respond(HttpStatusCode.Created, updated)
                else -> call.respond(HttpStatusCode.OK, updated)
            }
        }
    }

    open fun Route.deleteDefault(pathParam: String) {
        delete("/{$pathParam}") {
            val param = call.parameters[pathParam] ?: return@delete call.respond(HttpStatusCode.BadRequest)

            val id =
                service.getSingle { service.table.id eq param.toInt() }?.id
                    ?: return@delete call.respond(HttpStatusCode.NotFound)

            val removed = service.delete(id) { service.table.id eq param.toInt() }

            call.respond(if (removed) HttpStatusCode.OK else HttpStatusCode.NotFound)
        }
    }

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