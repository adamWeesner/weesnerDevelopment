import auth.UsersService
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.routing.*
import io.ktor.util.pipeline.PipelineContext
import shared.auth.HashedUser
import shared.auth.InvalidUserReason
import shared.base.GenericItem
import shared.base.GenericResponse
import shared.base.Response
import shared.base.Response.Companion.BadRequest
import shared.base.Response.Companion.NoContent
import shared.base.Response.Companion.NotFound
import shared.base.Response.Companion.Ok
import kotlin.reflect.KType

abstract class BaseRouter<I : GenericItem, S : Service<I>>(
    private val response: GenericResponse<I>,
    override val service: S,
    override val kType: KType
) : Router<I, S> {
    override fun Route.setupRoutes() {
        route("/$basePath") {
            addRequest()
            getRequest()
            updateRequest()
            deleteRequest()
        }
    }

    override fun Route.addRequest() {
        post {
            val body = call.receive<I>(kType)
            logRequest(body)

            val response =
                when (val addedItem = service.add(body)) {
                    null -> BadRequest("Failed to add $body.")
                    -1 -> Response.Conflict("Item with name $body already in database.")
                    else -> Response.Created("Added item to database with id $addedItem.")
                }

            respond(response)
        }
    }

    override fun Route.getRequest() {
        get {
            logRequest<I>()

            val itemId = call.request.queryParameters["id"]

            val items =
                (if (itemId == null) service.getAll()
                else listOf(service.get {
                    service.table.id eq itemId.toInt()
                }))?.filterNotNull()

            val response =
                if (items.isNullOrEmpty()) NoContent(response)
                else Ok(response.let {
                    it.items = items
                    it
                })

            respond(response)
        }
    }

    override fun Route.updateRequest() {
        put {
            val body = call.receive<I>(kType)
            logRequest(body)

            val response =
                if (body.id == null) {
                    BadRequest("Cannot update item without id")
                } else {
                    when (val updatedItem = service.update(body) {
                        service.table.id eq body.id!!
                    }) {
                        null -> BadRequest("Failed to update $body.")
                        else -> Ok("Updated item to database with id $updatedItem")
                    }
                }

            respond(response)
        }
    }

    override fun Route.deleteRequest() {
        delete {
            logRequest<I>()

            val itemId = call.request.queryParameters["id"]
                ?: return@delete respond(BadRequest("?id=(itemId) is needed."))

            val item = service.get { service.table.id eq itemId.toInt() }
                ?: return@delete respond(NotFound("Could not delete item with id $itemId"))

            val delete = service.delete(item) {
                service.table.id eq itemId.toInt()
            }

            val response =
                if (delete) Ok("Successfully removed item with id $itemId.")
                else NotFound("Could not delete item with id $itemId")

            respond(response)
        }
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.tokenAsUser(usersService: UsersService) =
    call.loggedUserData()?.getData()?.let {
        if (it.username == null || it.password == null) {
            respondErrorAuthorizing(InvalidUserReason.NoUserFound)
            return null
        } else {
            usersService.getUserFromHash(HashedUser(it.username, it.password))
        }
    }
