package com.weesnerdevelopment.service

import auth.Cipher
import auth.InvalidUserReason
import auth.JwtProvider
import auth.UserRouter
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.ryanharter.ktor.moshi.moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.weesnerdevelopment.AppConfig
import com.weesnerdevelopment.Path
import federalIncomeTax.FederalIncomeTaxRouter
import generics.route
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.jwt.jwt
import io.ktor.auth.parseAuthorizationHeader
import io.ktor.features.*
import io.ktor.http.HttpStatusCode
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.routing.Routing
import io.ktor.websocket.WebSockets
import medicare.MedicareRouter
import respondAuthorizationIssue
import respondServerError
import socialSecurity.SocialSecurityRouter
import taxWithholding.TaxWithholdingRouter
import java.time.Duration

class DatabaseServer {
    fun Application.main() {
        val appConfig = AppConfig(environment.config)
        val jwtProvider =
            JwtProvider(appConfig.issuer, appConfig.audience, appConfig.expiresIn, Cipher(appConfig.secret))

        install(DefaultHeaders)
        install(CallLogging)
        install(WebSockets)
        install(CORS) {
            allowCredentials = true
            host("${appConfig.baseUrl}:${appConfig.port}")
            maxAge = Duration.ofDays(1)
            allowNonSimpleContentTypes = true
        }

        install(ContentNegotiation) {
            moshi {
                add(KotlinJsonAdapterFactory())
            }
        }

        install(StatusPages) {
            exception<Throwable> { e ->
                when (e) {
                    is TokenExpiredException -> call.respondAuthorizationIssue(InvalidUserReason.Expired)
                    is JWTVerificationException -> call.respondAuthorizationIssue(InvalidUserReason.InvalidJwt)
                    else -> call.respondServerError(e)
                }
            }
            status(HttpStatusCode.Unauthorized) {
                try {
                    jwtProvider.decodeJWT((call.request.parseAuthorizationHeader() as HttpAuthHeader.Single).blob)
                } catch (e: Exception) {
                    return@status when (e) {
                        // usually happens when no token was passed...
                        is ClassCastException -> call.respondAuthorizationIssue(InvalidUserReason.InvalidJwt)
                        is TokenExpiredException -> call.respondAuthorizationIssue(InvalidUserReason.Expired)
                        is JWTVerificationException -> call.respondAuthorizationIssue(InvalidUserReason.InvalidJwt)
                        else -> call.respondServerError(Throwable(e))
                    }
                }

                call.respondAuthorizationIssue(InvalidUserReason.General)
            }
        }

        install(Authentication) {
            jwt {
                verifier(jwtProvider.verifier)
                this.realm = appConfig.realm
                validate { credential ->
                    if (credential.payload.audience.contains(appConfig.audience)) JWTPrincipal(credential.payload)
                    else null
                }
            }
        }

        DatabaseFactory.init()

        install(Routing) {
            val userRouter = UserRouter()
            val userService = userRouter.service as UsersService

            authenticate {
                route("/") {
                    handle {
                        val principal = call.authentication.principal<JWTPrincipal>()

                        principal!!.payload.claims.run {
                            val items = mutableMapOf<String, String?>()

                            forEach {
                                if (it.key.startsWith("attr-"))
                                    items[it.key.replace("attr-", "")] = it.value?.asString()
                            }

                            val userName = items["username"]
                            val password = items["password"]
                            val uuid = items["uuid"]

                            when {
                                userName != null && password != null -> {
                                    userService.getUserFromHash(HashedUser(userName, password))?.toJson()?.run {
                                        call.respond(this)
                                    } ?: call.respondAuthorizationIssue(InvalidUserReason.NoUserFound)
                                }
                                uuid != null -> {
                                    userService.getUserByUuid(uuid)?.toJson()?.run {
                                        call.respond(this)
                                    } ?: call.respondAuthorizationIssue(InvalidUserReason.NoUserFound)
                                }
                                else -> call.respondAuthorizationIssue(InvalidUserReason.General)
                            }
                        }
                    }
                }

                // user
                route(Path.User.me, userRouter)

                // tax fetcher
                route(Path.TaxFetcher.socialSecurity, SocialSecurityRouter())
                route(Path.TaxFetcher.medicare, MedicareRouter())
                route(Path.TaxFetcher.taxWithholding, TaxWithholdingRouter())
                route(Path.TaxFetcher.federalIncomeTax, FederalIncomeTaxRouter())
            }
        }
    }
}