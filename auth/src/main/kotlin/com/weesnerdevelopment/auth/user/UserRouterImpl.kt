package com.weesnerdevelopment.auth.user

import auth.JwtProvider
import auth.asToken
import com.weesnerdevelopment.businessRules.get
import com.weesnerdevelopment.businessRules.post
import com.weesnerdevelopment.businessRules.put
import com.weesnerdevelopment.businessRules.respond
import com.weesnerdevelopment.shared.auth.HashedUser
import com.weesnerdevelopment.shared.auth.TokenResponse
import com.weesnerdevelopment.shared.auth.User
import com.weesnerdevelopment.shared.base.ServerError
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.routing.*
import java.util.*

@OptIn(KtorExperimentalLocationsAPI::class)
data class UserRouterImpl(
    private val repo: UserRepository,
    private val jwtProvider: JwtProvider
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

    override fun setup(routing: Routing) {
        routing.apply {
            authenticate {
                get<UserAccountEndpoint> {
                    val id = getBearerUuid()

                    if (id == null)
                        return@get respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Invalid id '$id' attempting to get account."
                            )
                        )

                    when (val foundUser = repo.account(id)) {
                        null -> {
                            return@get respond(
                                HttpStatusCode.NotFound,
                                ServerError(
                                    HttpStatusCode.NotFound.description,
                                    HttpStatusCode.NotFound.value,
                                    "No account with id '$id' found."
                                )
                            )
                        }
                        else -> {
                            return@get respond(HttpStatusCode.OK, foundUser.redact)
                        }
                    }
                }
            }

            get<UserEndpoint> {
                val username = call.username
                val password = call.password

                if (!username.isNullOrBlank() && !password.isNullOrBlank()) {
                    val user = repo.login(HashedUser(username, password))
                    if (user == null) {
                        return@get respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Invalid login credentials."
                            )
                        )
                    } else {
                        val jwtToken = user.asToken(jwtProvider)
                        return@get respond(HttpStatusCode.OK, TokenResponse(jwtToken))
                    }
                }

                val id = call.userId

                if (id.isNullOrBlank()) {
                    return@get respond(
                        HttpStatusCode.BadRequest,
                        ServerError(
                            HttpStatusCode.BadRequest.description,
                            HttpStatusCode.BadRequest.value,
                            "?id={id} needed to get account."
                        )
                    )
                }

                val idAsUuid = runCatching { UUID.fromString(id) }.getOrNull()

                if (idAsUuid == null)
                    return@get respond(
                        HttpStatusCode.BadRequest,
                        ServerError(
                            HttpStatusCode.BadRequest.description,
                            HttpStatusCode.BadRequest.value,
                            "Invalid id '$id' attempting to get account."
                        )
                    )

                when (val foundUser = repo.account(idAsUuid)) {
                    null -> {
                        return@get respond(
                            HttpStatusCode.NotFound,
                            ServerError(
                                HttpStatusCode.NotFound.description,
                                HttpStatusCode.NotFound.value,
                                "No account with id '$idAsUuid' found."
                            )
                        )
                    }
                    else -> {
                        val jwtToken = foundUser.asToken(jwtProvider)
                        return@get respond(HttpStatusCode.OK, TokenResponse(jwtToken))
                    }
                }
            }

            post<UserEndpoint, User> { user ->
                if (user == null)
                    return@post respond(
                        HttpStatusCode.BadRequest,
                        ServerError(
                            HttpStatusCode.BadRequest.description,
                            HttpStatusCode.BadRequest.value,
                            "Cannot add invalid user."
                        )
                    )

                val newUser = repo.create(user)
                val token = newUser.asToken(jwtProvider)
                return@post respond(HttpStatusCode.Created, TokenResponse(token))
            }

            authenticate {
                put<UserEndpoint, User> { user ->
                    if (user == null)
                        return@put respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Cannot update invalid user."
                            )
                        )

                    val updatedUser = repo.update(user)
                    if (updatedUser == null)
                        return@put respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "An error occurred attempting to update user."
                            )
                        )

                    return@put respond(HttpStatusCode.OK, updatedUser)
                }

                delete<UserEndpoint> {
                    val id = call.userId
                    val authUuid = getBearerUuid()!!

                    if (id.isNullOrBlank()) {
                        return@delete respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Invalid id '$id' attempting to delete user."
                            )
                        )
                    }

                    val idAsUuid = runCatching { UUID.fromString(id) }.getOrNull()

                    if (idAsUuid == null)
                        return@delete respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Invalid id '$id' attempting to delete user."
                            )
                        )

                    when (val deletedUser = repo.delete(authUuid)) {
                        false -> return@delete respond(
                            HttpStatusCode.NotFound,
                            ServerError(
                                HttpStatusCode.NotFound.description,
                                HttpStatusCode.NotFound.value,
                                "No user with id '$idAsUuid' found."
                            )
                        )
                        else -> return@delete respond(HttpStatusCode.OK, deletedUser)
                    }
                }
            }
        }
    }
}