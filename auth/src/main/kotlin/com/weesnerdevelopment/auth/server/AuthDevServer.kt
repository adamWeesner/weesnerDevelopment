package com.weesnerdevelopment.auth.server

import auth.CustomPrincipal
import auth.JwtProvider
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.weesnerdevelopment.auth.routes
import com.weesnerdevelopment.businessRules.AppConfig
import com.weesnerdevelopment.businessRules.Log
import com.weesnerdevelopment.businessRules.Server
import com.weesnerdevelopment.shared.Paths
import com.weesnerdevelopment.shared.auth.InvalidUserReason
import com.weesnerdevelopment.shared.base.Response
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.serialization.*
import kimchi.Kimchi
import kotlinx.serialization.ExperimentalSerializationApi
import logging.StdOutLogger
import org.kodein.di.generic.instance
import org.kodein.di.ktor.kodein
import respond
import respondErrorAuthorizing
import respondErrorServer
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class, ExperimentalSerializationApi::class)
object AuthDevServer : Server {
    override fun start(app: Application) {
        with(app) {
            val appConfig by kodein().instance<AppConfig>()
            val jwtProvider by kodein().instance<JwtProvider>()

            Kimchi.addLog(StdOutLogger)

            install(DefaultHeaders) {
                header(HttpHeaders.AcceptCharset, Charsets.UTF_8.toString())
                header(
                    HttpHeaders.Accept,
                    ContentType.Application.Json.withParameter("charset", Charsets.UTF_8.toString()).toString()
                )
            }
            install(CallLogging)
            install(CORS) {
                method(HttpMethod.Options)
                header(HttpHeaders.ContentType)
                header(HttpHeaders.Authorization)
                host("${appConfig.baseUrl}:${appConfig.sslPort}", schemes = listOf("https"))
                host(appConfig.baseUrl, schemes = listOf("https"))
                host("${appConfig.baseUrl}:${appConfig.port}", schemes = listOf("http"))
                host("localhost:3000")
                maxAgeDuration = Duration.days(1)
                allowCredentials = true
                allowNonSimpleContentTypes = true
            }
            install(ContentNegotiation) {
                json(com.weesnerdevelopment.shared.json {
                    prettyPrint = true
                    prettyPrintIndent = "  "
                    isLenient = true
                })
            }
            install(StatusPages) {
                exception<Throwable> { e ->
                    when (e) {
                        is TokenExpiredException -> respondErrorAuthorizing(InvalidUserReason.Expired)
                        is JWTVerificationException -> respondErrorAuthorizing(InvalidUserReason.InvalidJwt)
                        else -> respondErrorServer(e)
                    }
                }
                status(HttpStatusCode.Unauthorized) {
                    try {
                        jwtProvider.decodeJWT((call.request.parseAuthorizationHeader() as HttpAuthHeader.Single).blob)
                    } catch (e: Exception) {
                        return@status when (e) {
                            // usually happens when no token was passed...
                            is ClassCastException -> respondErrorAuthorizing(InvalidUserReason.InvalidJwt)
                            is TokenExpiredException -> respondErrorAuthorizing(InvalidUserReason.Expired)
                            is JWTVerificationException -> respondErrorAuthorizing(InvalidUserReason.InvalidJwt)
                            else -> respondErrorAuthorizing(InvalidUserReason.General)
                        }
                    }

                    respondErrorAuthorizing(InvalidUserReason.General)
                }
            }
            install(Authentication) {
                jwt {
                    verifier(jwtProvider.verifier)
                    this.realm = appConfig.realm
                    validate { credential ->
                        Log.debug("credential $credential")
                        if (credential.payload.audience.contains(appConfig.audience)) CustomPrincipal(credential.payload)
                        else null
                    }
                }
            }
            install(Locations)
            install(Routing) {
                route(Paths.User.health) {
                    get {
                        respond(
                            Response.Ok(
                                "Auth ${this.call.request.path().replace("/health", "")} is up and running"
                            )
                        )
                    }
                }

                routes()
            }
        }
    }
}