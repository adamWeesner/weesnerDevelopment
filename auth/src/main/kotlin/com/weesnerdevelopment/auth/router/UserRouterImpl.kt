package com.weesnerdevelopment.auth.router

import auth.AuthValidator
import auth.JwtProvider
import com.weesnerdevelopment.auth.endpoints.UserAccountEndpoint
import com.weesnerdevelopment.auth.endpoints.UserEndpoint
import com.weesnerdevelopment.auth.endpoints.UserInfoEndpoint
import com.weesnerdevelopment.auth.repository.UserRepository
import com.weesnerdevelopment.auth.repository.firebase.UserRepositoryFirebase
import com.weesnerdevelopment.businessRules.auth.parseAuthorizationToken
import com.weesnerdevelopment.businessRules.get
import com.weesnerdevelopment.businessRules.post
import com.weesnerdevelopment.businessRules.put
import com.weesnerdevelopment.businessRules.respond
import com.weesnerdevelopment.shared.auth.TokenUser
import com.weesnerdevelopment.shared.auth.User
import com.weesnerdevelopment.shared.base.Response
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.locations.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
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
    private val ApplicationCall.email
        get() = request.queryParameters[UserEndpoint::email.name]

    /**
     * Reduces typing to get the param for `?password=` :)
     */
    private val ApplicationCall.password
        get() = request.queryParameters[UserEndpoint::password.name]

    /**
     * for firebase this needs to be the bearer token
     */
    private fun PipelineContext<Unit, ApplicationCall>.getIdentifier(): String {
        val id = authValidator.getUuid(this)
        val token = call.request.parseAuthorizationToken() ?: ""

        return if (repo is UserRepositoryFirebase) token else id
    }

    override fun setup(routing: Routing) {
        routing.apply {
            authenticate {
                // get another account info
                get<UserInfoEndpoint> {
                    val id = getIdentifier()

                    val response = repo.info(id)
                    return@get when {
                        response.isSuccess -> {
                            val user = response.getOrNull()
                            if (user == null)
                                respond(Response.BadRequest("Cannot get user info."))
                            else
                                respond(Response.Ok(user))
                        }
                        else -> {
                            respond(Response.NotFound("No account with id '$id' found."))
                        }
                    }
                }
            }

            // login
            get<UserEndpoint> {
                val email = call.email
                val password = call.password

                if (!email.isNullOrBlank() && !password.isNullOrBlank()) {
                    val response = repo.login(email, password)
                    return@get when {
                        response.isSuccess -> {
                            val user = response.getOrNull()
                            if (user == null)
                                respond(Response.BadRequest("Cannot log in user."))
                            else
                                respond(Response.Ok(user))
                        }
                        else -> {
                            respond(Response.BadRequest("Invalid login credentials."))
                        }
                    }
                }

                val id = call.userId

                if (id.isNullOrBlank())
                    return@get respond(Response.BadRequest("?id={id} needed to get account."))

                // for firebase this needs to be the bearer token
                val response = repo.account(getIdentifier())
                return@get when {
                    response.isSuccess -> {
                        val user = response.getOrNull()
                        if (user == null)
                            respond(Response.BadRequest("Cannot get user info."))
                        else
                            respond(Response.Created(user))
                    }
                    else -> {
                        respond(Response.NotFound("No account with id '$id' found."))
                    }
                }
            }

            // create account
            post<UserEndpoint, User> { user ->
                if (user == null)
                    return@post respond(Response.BadRequest("Cannot add invalid user."))

                val response = repo.create(user)

                return@post when {
                    response.isSuccess -> {
                        val newUser = response.getOrNull()
                        if (newUser == null)
                            respond(Response.BadRequest("Cannot add invalid user."))
                        else
                            respond(Response.Created(newUser))
                    }
                    else -> {
                        respond(Response.BadRequest("Cannot add invalid user."))
                    }
                }
            }

            authenticate {
                // get user info
                get<UserAccountEndpoint> {
                    val id = getIdentifier()

                    val response = repo.account(id)
                    return@get when {
                        response.isSuccess -> {
                            val user = response.getOrNull()
                            if (user == null)
                                respond(Response.BadRequest("Cannot get user info."))
                            else
                                respond(Response.Ok(user))
                        }
                        else -> {
                            respond(Response.NotFound("No account with id '$id' found."))
                        }
                    }
                }

                // update user info
                put<UserEndpoint, TokenUser> { user ->
                    if (user == null)
                        return@put respond(Response.BadRequest("Cannot update invalid user."))

                    val response = repo.update(user)
                    return@put when {
                        response.isSuccess -> {
                            val tokenUser = response.getOrNull()
                            if (tokenUser == null)
                                respond(Response.BadRequest("Cannot update invalid user."))
                            else
                                respond(Response.Ok(tokenUser))
                        }
                        else -> {
                            respond(Response.BadRequest("Cannot update invalid user."))
                        }
                    }
                }

                // delete account
                delete<UserEndpoint> {
                    val id = call.userId
                    val authUuid = getIdentifier()

                    if (id.isNullOrBlank())
                        return@delete respond(Response.BadRequest("Invalid id '$id' attempting to delete user."))

                    return@delete when (repo.delete(authUuid)) {
                        false -> respond(Response.NotFound("No user with id '$authUuid' found."))
                        else -> respond(Response.Ok(true.toString()))
                    }
                }
            }
        }
    }
}