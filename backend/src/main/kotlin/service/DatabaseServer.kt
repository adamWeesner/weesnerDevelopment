package com.weesnerdevelopment.service

import auth.*
import category.CategoryRouter
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.ryanharter.ktor.moshi.moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.weesnerdevelopment.Paths.*
import com.weesnerdevelopment.respondAuthorizationIssue
import com.weesnerdevelopment.respondServerError
import com.weesnerdevelopment.toJson
import federalIncomeTax.FederalIncomeTaxRouter
import generics.route
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.jwt.jwt
import io.ktor.auth.parseAuthorizationHeader
import io.ktor.features.*
import io.ktor.http.HttpStatusCode
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.route
import io.ktor.websocket.WebSockets
import medicare.MedicareRouter
import socialSecurity.SocialSecurityRouter
import taxWithholding.TaxWithholdingRouter
import java.time.Duration
import kotlin.collections.set

class DatabaseServer {
    fun Application.main() {
        val issuer = environment.config.property("jwt.domain").getString()
        val audience = environment.config.property("jwt.audience").getString()
        val realm = environment.config.property("jwt.realm").getString()

        val jwtVerifier = makeJwtVerifier(issuer, audience)

        install(DefaultHeaders)
        install(CallLogging)
        install(WebSockets)
        install(CORS) {
            allowCredentials = true
            host("weesnerdevelopment.com", subDomains = listOf("api"))
            host("localhost:3000")
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
                jwtVerifier.verify((call.request.parseAuthorizationHeader() as HttpAuthHeader.Single).blob)

                call.respondAuthorizationIssue(InvalidUserReason.General)
            }
        }

        install(Authentication) {
            jwt {
                verifier(jwtVerifier)
                this.realm = realm
                validate { credential ->
                    if (credential.payload.audience.contains(audience)) JWTPrincipal(credential.payload)
                    else null
                }
            }
        }

        DatabaseFactory.init()

        install(Routing) {
            val userService: UsersService
            route("/user", UserRouter().also {
                userService = it.service as UsersService
            })

            authenticate {
                route("/whoAmI") {
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
                                userName != null && password != null ->
                                    call.respond(userService.getUserFromHash(HashedUser(userName, password)).toJson())
                                uuid != null -> call.respond(userService.getUserByUuid(uuid).toJson())
                                else -> call.respondAuthorizationIssue(InvalidUserReason.General)
                            }
                        }
                    }
                }
            }

            // tax fetcher
            route(socialSecurity.name, SocialSecurityService()) { SocialSecurityResponse(it) }
            route(medicare.name, MedicareService()) { MedicareResponse(it) }
            route(taxWithholding.name, TaxWithholdingService()) { TaxWithholdingResponse(it) }
            route(federalIncomeTax.name, FederalIncomeTaxService()) { FederalIncomeTaxResponse(it) }
            // bill man
            route(category.name, CategoriesService()) { CategoriesResponse(it) }
        }
    }
}