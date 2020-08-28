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
import shared.base.OwnedItem
import shared.base.Response
import shared.base.Response.Companion.BadRequest
import shared.base.Response.Companion.NoContent
import shared.base.Response.Companion.NotFound
import shared.base.Response.Companion.Ok
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf

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
            val userInfo = getUserInfo()

            logRequest(body)

            if (userInfo == null && isOwnedItem)
                return@post respondErrorAuthorizing(InvalidUserReason.InvalidUserInfo)

            if (body is OwnedItem) {
                val notYourItem = when (userInfo?.first) {
                    "username" -> body.owner.username != userInfo.second
                    "uuid" -> body.owner.uuid != userInfo.second
                    else -> false
                }

                if (notYourItem)
                    return@post respond(BadRequest("Failed to add $body."))
            }

            val response = when (val addedItem = service.add(body)) {
                null -> BadRequest("Failed to add $body.")
                -1 -> Response.Conflict("Item with name $body already in database.")
                else -> Response.Created("Added item to database with id $addedItem.")
            }

            respond(response)
        }
    }

    override fun Route.getRequest() {
        get {
            val userInfo = getUserInfo()

            logRequest<I>()

            if (userInfo == null && isOwnedItem)
                return@get respondErrorAuthorizing(InvalidUserReason.InvalidUserInfo)

            val itemId = call.request.queryParameters["id"]

            val items =
                (if (itemId == null) service.getAll()
                else listOf(service.get {
                    service.table.id eq itemId.toInt()
                }))?.filterNotNull()


            val filteredItems = when (isOwnedItem) {
                true -> {
                    when (userInfo?.first) {
                        "username" -> items?.filter { (it as OwnedItem).owner.username == userInfo.second }
                        "uuid" -> items?.filter { (it as OwnedItem).owner.uuid == userInfo.second }
                        else -> items
                    }
                }
                false -> items
            }

            val response =
                if (filteredItems.isNullOrEmpty()) NoContent(response)
                else Ok(response.let {
                    it.items = filteredItems
                    it
                })

            respond(response)
        }
    }

    override fun Route.updateRequest() {
        put {
            val body = call.receive<I>(kType)
            val userInfo = getUserInfo()

            logRequest(body)

            if (body is OwnedItem) {
                val notYourItem = when (userInfo?.first) {
                    "username" -> body.owner.username != userInfo.second
                    "uuid" -> body.owner.uuid != userInfo.second
                    else -> false
                }

                if (notYourItem)
                    return@put respond(BadRequest("Failed to update $body."))
            }

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
            val userInfo = getUserInfo()

            logRequest<I>()

            if (userInfo == null && isOwnedItem)
                return@delete respondErrorAuthorizing(InvalidUserReason.InvalidUserInfo)

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

    private val isOwnedItem = kType.isSubtypeOf(OwnedItem::class.createType())
}

fun PipelineContext<Unit, ApplicationCall>.getUserInfo() = call.loggedUserData()?.getData()?.let {
    when {
        it.username != null -> "username" to it.username
        it.uuid != null -> "uuid" to it.uuid
        else -> null
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
