package com.weesnerdevelopment.auth.user

import auth.AuthValidator
import auth.JwtProvider
import auth.asToken
import com.weesnerdevelopment.businessRules.get
import com.weesnerdevelopment.businessRules.post
import com.weesnerdevelopment.businessRules.put
import com.weesnerdevelopment.businessRules.respond
import com.weesnerdevelopment.businessRules.respondWithError
import com.weesnerdevelopment.shared.auth.HashedUser
import com.weesnerdevelopment.shared.auth.TokenResponse
import com.weesnerdevelopment.shared.auth.User
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.routing.*
import java.util.*

@OptIn(KtorExperimentalLocationsAPI::class)
data class UserRouterImpl(
    private val repo: UserRepository,
    private val jwtProvider: JwtProvider,
    private val authValidator: AuthValidator
) : UserRouter {
    /**
     * Reduces typing to get the param for `?id=` :)
     */
    private val ApplicationCall.userId
        get() = request.queryParameters[UserEndpoint::id.name]

    /**
     * Reduces typing to get the param for `?username=` :)
     */
    private val ApplicationCall.username
        get() = request.queryParameters[UserEndpoint::username.name]

    /**
     * Reduces typing to get the param for `?password=` :)
     */
    private val ApplicationCall.password
        get() = request.queryParameters[UserEndpoint::password.name]

    private fun hasValidCredentials(username: String?, password: String?): Boolean {
        val parsedUsername = runCatching { Base64.getDecoder().decode(username) }.getOrNull()
        val parsedPassword = runCatching { Base64.getDecoder().decode(password) }.getOrNull()

        return parsedUsername != null && parsedPassword != null
    }

    override fun setup(routing: Routing) {
        routing.apply {
            authenticate {
                get<UserInfoEndpoint> {
                    val id = authValidator.getUuid(this)

                    return@get when (val foundUser = repo.info(id)) {
                        null -> respondWithError(HttpStatusCode.NotFound, "No account with id '$id' found.")
                        else -> respond(HttpStatusCode.OK, foundUser.redact)
                    }
                }
            }

            get<UserEndpoint> {
                val username = call.username
                val password = call.password

                if (!username.isNullOrBlank() && !password.isNullOrBlank() && hasValidCredentials(username, password)) {
                    val user = repo.login(HashedUser(username, password))
                    return@get if (user == null)
                        respondWithError(HttpStatusCode.BadRequest, "Invalid login credentials.")
                    else
                        respond(HttpStatusCode.OK, TokenResponse(user.asToken(jwtProvider)))
                }

                val id = call.userId

                if (id.isNullOrBlank())
                    return@get respondWithError(HttpStatusCode.BadRequest, "?id={id} needed to get account.")

                return@get when (val foundUser = repo.account(id)) {
                    null -> respondWithError(HttpStatusCode.NotFound, "No account with id '$id' found.")
                    else -> respond(HttpStatusCode.OK, TokenResponse(foundUser.asToken(jwtProvider)))
                }
            }

            post<UserEndpoint, User> { user ->
                if (user == null || !hasValidCredentials(user.username, user.password))
                    return@post respondWithError(HttpStatusCode.BadRequest, "Cannot add invalid user.")

                return@post when (val newUser = repo.create(user)) {
                    null -> respondWithError(HttpStatusCode.BadRequest, "Cannot add invalid user.")
                    else -> respond(HttpStatusCode.Created, TokenResponse(user.asToken(jwtProvider)))
                }
            }

            authenticate {
                get<UserAccountEndpoint> {
                    val id = authValidator.getUuid(this)

                    return@get when (val foundUser = repo.account(id)) {
                        null -> respondWithError(HttpStatusCode.NotFound, "No account with id '$id' found.")
                        else -> respond(HttpStatusCode.OK, foundUser.redact)
                    }
                }

                put<UserEndpoint, User> { user ->
                    if (user == null || !hasValidCredentials(user.username, user.password))
                        return@put respondWithError(HttpStatusCode.BadRequest, "Cannot update invalid user.")

                    return@put when (val updatedUser = repo.update(user)) {
                        null -> respondWithError(
                            HttpStatusCode.BadRequest,
                            "An error occurred attempting to update user."
                        )
                        else -> respond(HttpStatusCode.OK, updatedUser)
                    }
                }

                delete<UserEndpoint> {
                    val id = call.userId
                    val authUuid = authValidator.getUuid(this)

                    if (id.isNullOrBlank())
                        return@delete respondWithError(
                            HttpStatusCode.BadRequest,
                            "Invalid id '$id' attempting to delete user."
                        )

                    return@delete when (val deletedUser = repo.delete(authUuid)) {
                        false -> respondWithError(HttpStatusCode.NotFound, "No user with id '$authUuid' found.")
                        else -> respond(HttpStatusCode.OK, deletedUser)
                    }
                }
            }
        }
    }
}