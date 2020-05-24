package auth

import generics.*
import history.HistoryService
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.request.receive
import io.ktor.response.header
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.put
import io.ktor.util.pipeline.PipelineContext
import loggedUserData
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import respond
import respondErrorAuthorizing
import shared.auth.HashedUser
import shared.auth.User
import java.util.*

class UserRouter(
    basePath: String,
    private val usersService: UsersService,
    private val historyService: HistoryService,
    private val jwtProvider: JwtProvider,
    private val accountUrl: String
) : GenericRouter<User, UsersTable>(
    basePath,
    usersService,
    UsersResponse()
) {
    override suspend fun postQualifier(receivedItem: User) =
        service.getSingle { service.table.uuid eq receivedItem.uuid!! }

    override fun singleEq(param: String) = service.table.uuid eq param

    override suspend fun putQualifier(receivedItem: User) =
        service.update(receivedItem) { service.table.uuid eq receivedItem.uuid!! }

    override fun Route.getSingle() {
        authenticate {
            get("/$accountUrl") {
                call.loggedUserData()?.getData()?.apply {
                    when {
                        username != null && password != null -> {
                            val user = usersService.getUserFromHash(HashedUser(username, password))
                                ?: return@get call.respondErrorAuthorizing(InvalidUserReason.NoUserFound)

                            call.respond(Ok(user.redacted()))
                        }
                        uuid != null -> {
                            val user = usersService.getUserByUuid(uuid)
                                ?: return@get call.respondErrorAuthorizing(InvalidUserReason.NoUserFound)

                            call.respond(Ok(user.redacted()))
                        }
                        else -> call.respondErrorAuthorizing(InvalidUserReason.General)
                    }
                }
            }
        }
    }

    override fun Route.putDefault() {
        authenticate {
            put("/") {
                val authToken = call.loggedUserData()
                val item = call.receive<User>()

                if (authToken?.getData()?.uuid != item.uuid) return@put call.respondErrorAuthorizing(InvalidUserReason.WrongUser)

                val updated = putQualifier(item)

                when {
                    updated == null -> call.respond(BadRequest("Error occurred updated user information."))
                    updated.id != item.id -> call.respond(Created(updated.redacted()))
                    else -> call.respond(Ok(updated.redacted()))
                }
            }
        }
    }

    override suspend fun PipelineContext<Unit, ApplicationCall>.putAdditional(
        item: User,
        updatedItem: User
    ): User? {
        val history = handleHistory(item, updatedItem, usersService, historyService)
        return updatedItem.copy(history = history)
    }

    fun Route.login(pathParam: String) {
        post(pathParam) {
            val credentials = call.receive<HashedUser>()

            hashedUser(credentials.username, credentials.password) {
                it?.run {
                    val generatedToken = asHashed()?.asToken(jwtProvider) ?: throw Exception("Generate token was null")

                    call.response.header("x-auth-token", generatedToken)
                    call.respond(Ok(TokenResponse(generatedToken)))
                } ?: call.respondErrorAuthorizing(InvalidUserReason.NoUserFound)
            }
        }
    }

    fun Route.signUp(pathParam: String) {
        post(pathParam) {
            val credentials = call.receive<User>()

            hashedUser(credentials.username, credentials.password) {
                if (it != null) return@hashedUser call.respond(Conflict("Invalid user credentials."))

                val newUser = User(
                    uuid = UUID.nameUUIDFromBytes("${credentials.username}${credentials.password}".toByteArray())
                        .toString(),
                    email = credentials.email,
                    name = credentials.name,
                    photoUrl = credentials.photoUrl,
                    username = credentials.username,
                    password = credentials.password
                )

                when (val added = service.add(newUser)) {
                    null -> call.respond(Conflict("Unable to save user credentials."))
                    else -> call.respond(Created(TokenResponse(added.asHashed()?.asToken(jwtProvider))))
                }
            }
        }
    }

    /**
     * Helper function for getting the hashed user information from the request.
     */
    private suspend fun PipelineContext<Unit, ApplicationCall>.hashedUser(
        username: String?,
        password: String?,
        tryBlock: suspend (hashedUser: User?) -> Unit
    ) {
        if (username == null || password == null) {
            call.respond(BadRequest("Username and password must be provided"))
            return
        }

        val hashedUser = HashedUser(username, password)

        if (hashedUser.checkValidity() != null) {
            call.respondErrorAuthorizing(InvalidUserReason.InvalidUserInfo)
            return
        }

        try {
            val response = (service as UsersService).getUserFromHash(hashedUser)

            tryBlock(response)
        } catch (e: Exception) {
            call.respond(BadRequest(e.message ?: "Unknown reason for request failure"))
        }
    }
}
