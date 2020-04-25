package auth

import generics.GenericRouter
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.put
import io.ktor.util.pipeline.PipelineContext
import loggedUserData
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import respondAuthorizationIssue
import shared.auth.HashedUser
import shared.auth.User
import java.util.*
import kotlin.reflect.KType

class UserRouter(
    private val jwtProvider: JwtProvider,
    accountUrl: String
) : GenericRouter<User, UsersTable>(
    UsersService(),
    UsersResponse()
) {
    override val getParamName = accountUrl
    override val deleteParamName = "uuid"
    override val putParamUrl = accountUrl

    override suspend fun postQualifier(receivedItem: User) =
        service.getSingle { service.table.uuid eq receivedItem.uuid }

    override fun deleteEq(param: String) = service.table.uuid eq param

    override suspend fun putQualifier(receivedItem: User) =
        service.update(receivedItem) { service.table.uuid eq receivedItem.uuid }

    override fun Route.getSingle(pathParam: String) {
        authenticate {
            get("/$pathParam") {
                call.loggedUserData()?.getData()?.apply {
                    when {
                        username != null && password != null -> {
                            (service as UsersService).getUserFromHash(HashedUser(username, password))?.run {
                                call.respond(HttpStatusCode.OK, this.redacted() ?: "")
                            } ?: call.respondAuthorizationIssue(InvalidUserReason.NoUserFound)
                        }
                        uuid != null -> {
                            (service as UsersService).getUserByUuid(uuid)?.run {
                                call.respond(HttpStatusCode.OK, this.redacted() ?: "")
                            } ?: call.respondAuthorizationIssue(InvalidUserReason.NoUserFound)
                        }
                        else -> call.respondAuthorizationIssue(InvalidUserReason.General)
                    }
                }
            }
        }
    }

    override fun Route.putDefault(type: KType) {
        authenticate {
            put("/$putParamUrl") {
                val authToken = call.loggedUserData()
                val item = call.receive<User>(type)

                if (authToken?.getData()?.uuid != item.uuid) return@put call.respond(HttpStatusCode.Unauthorized)

                val updated = putQualifier(item)

                when {
                    updated == null -> call.respond(HttpStatusCode.BadRequest)
                    updated.id != item.id -> call.respond(HttpStatusCode.Created, updated.redacted() ?: "")
                    else -> call.respond(HttpStatusCode.OK, updated.redacted() ?: "")
                }
            }
        }
    }

    fun Route.login(pathParam: String) {
        post(pathParam) {
            val credentials = call.receive<HashedUser>()

            hashedUser(credentials.username, credentials.password) {
                it?.run {
                    val generatedToken =
                        asHashed()?.asToken(jwtProvider) ?: throw Exception("Generate token was null")

                    call.response.header("x-auth-token", generatedToken)
                    call.respond(HttpStatusCode.OK, TokenResponse(generatedToken))
                } ?: call.respondAuthorizationIssue(InvalidUserReason.NoUserFound)
            }
        }
    }

    fun Route.signUp(pathParam: String) {
        post(pathParam) {
            val credentials = call.receive<User>()

            hashedUser(credentials.username, credentials.password) {
                if (it != null) return@hashedUser call.respond(HttpStatusCode.Conflict)

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
                    null -> call.respond(HttpStatusCode.Conflict)
                    else -> call.respond(HttpStatusCode.Created, TokenResponse(added.asHashed()?.asToken(jwtProvider)))
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
            call.respond(HttpStatusCode.BadRequest, "Username and password must be provided")
            return
        }

        val hashedUser = HashedUser(username, password)

        if (hashedUser.checkValidity() != null) {
            call.respondAuthorizationIssue(InvalidUserReason.InvalidUserInfo)
            return
        }

        try {
            val response = (service as UsersService).getUserFromHash(hashedUser)

            tryBlock(response)
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, e.message ?: "Unknown reason for request failure")
        }
    }
}
