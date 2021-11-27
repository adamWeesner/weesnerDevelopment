package auth

import BaseRouter
import com.weesnerdevelopment.shared.auth.HashedUser
import com.weesnerdevelopment.shared.auth.InvalidUserReason
import com.weesnerdevelopment.shared.auth.TokenResponse
import com.weesnerdevelopment.shared.auth.User
import com.weesnerdevelopment.shared.base.GenericResponse
import com.weesnerdevelopment.shared.base.Response.Companion.BadRequest
import com.weesnerdevelopment.shared.base.Response.Companion.Conflict
import com.weesnerdevelopment.shared.base.Response.Companion.Created
import com.weesnerdevelopment.shared.base.Response.Companion.Ok
import com.weesnerdevelopment.shared.toJson
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import logRequest
import loggedUserData
import respond
import respondErrorAuthorizing
import java.util.*
import kotlin.reflect.full.createType

class UserRouter(
    override val basePath: String,
    override val service: UsersService,
    private val jwtProvider: JwtProvider,
    private val accountUrl: String,
    private val loginUrl: String,
    private val signUpUrl: String
) : BaseRouter<User, UsersService>(
    UsersResponse(),
    service,
    User::class.createType()
) {
    override fun GenericResponse<User>.parse(): String = this.toJson()

    override fun Route.setupRoutes() {
        route(basePath) {
            getRequest()
            updateRequest()
            login(loginUrl)
            signUp(signUpUrl)
        }
    }

    override fun Route.getRequest() {
        authenticate {
            get("/$accountUrl") {
                logRequest<User>()

                val auth = call.loggedUserData()?.getData()
                    ?: return@get respondErrorAuthorizing(InvalidUserReason.General)

                val user = when {
                    auth.username != null && auth.password != null ->
                        service.getUserFromHash(HashedUser(auth.username, auth.password))
                    auth.uuid != null ->
                        service.getUserByUuid(auth.uuid)
                    else -> null
                }

                user?.redacted()?.let {
                    respond(Ok(it))
                } ?: return@get respondErrorAuthorizing(InvalidUserReason.NoUserFound)
            }
        }
    }

    override fun Route.updateRequest() {
        authenticate {
            put("/") {
                val authToken = call.loggedUserData()?.getData()

                val item = call.receive<User>()
                logRequest(item)

                val tokenToHash = authToken?.let {
                    if (it.username == null)
                        return@put respondErrorAuthorizing(InvalidUserReason.InvalidUserInfo)

                    if (it.password == null)
                        return@put respondErrorAuthorizing(InvalidUserReason.InvalidUserInfo)

                    HashedUser(it.username, it.password)
                } ?: return@put respondErrorAuthorizing(InvalidUserReason.NoUserFound)

                val hashedUser = service.getUserFromHash(tokenToHash)

                if (hashedUser?.uuid != item.uuid)
                    return@put respondErrorAuthorizing(InvalidUserReason.WrongUser)

                val updated = service.update(item.copy(id = hashedUser?.id)) {
                    service.table.id eq hashedUser?.id!!
                }

                val updatedUserInfo = service.getUserByUuidRedacted(item.uuid ?: "")

                val response = when {
                    updated == null -> BadRequest("Error occurred updated user information.")
                    updated != item.id -> Created(updatedUserInfo.toJson())
                    else -> Ok(updatedUserInfo.toJson())
                }

                respond(response)
            }
        }
    }

    private fun Route.login(pathParam: String) {
        post(pathParam) {
            val credentials = call.receive<HashedUser>()
            logRequest<User>()

            hashedUser(credentials.username, credentials.password) {
                it?.run {
                    val generatedToken = asHashed()?.asToken(jwtProvider) ?: throw Exception("Generate token was null")

                    call.response.header("x-auth-token", generatedToken)
                    respond(Ok(TokenResponse(generatedToken).toJson()))
                } ?: respondErrorAuthorizing(InvalidUserReason.NoUserFound)
            }
        }
    }

    private fun Route.signUp(pathParam: String) {
        post(pathParam) {
            val credentials = call.receive<User>()
            logRequest(credentials)

            hashedUser(credentials.username, credentials.password) {
                if (it != null) return@hashedUser respond(Conflict("Invalid user credentials."))

                val newUser = User(
                    uuid = UUID.nameUUIDFromBytes("${credentials.username}${credentials.password}".toByteArray())
                        .toString(),
                    email = credentials.email,
                    name = credentials.name,
                    photoUrl = credentials.photoUrl,
                    username = credentials.username,
                    password = credentials.password
                )

                val response = when (val added = service.addUser(newUser)) {
                    null -> Conflict("Unable to save user credentials.")
                    else -> Created(TokenResponse(added.asHashed()?.asToken(jwtProvider)).toJson())
                }

                respond(response)
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
            respond(BadRequest("Username and password must be provided."))
            return
        }

        val hashedUser = HashedUser(username, password)

        if (hashedUser.checkValidity() != null) {
            respondErrorAuthorizing(InvalidUserReason.InvalidUserInfo)
            return
        }

        try {
            val response = service.getUserFromHash(hashedUser)

            tryBlock(response)
        } catch (e: Exception) {
            respond(BadRequest(e.message ?: "Unknown reason for request failure."))
        }
    }
}
