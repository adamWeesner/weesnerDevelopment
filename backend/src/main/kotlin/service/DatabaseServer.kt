package com.weesnerdevelopment.service

import auth.CustomPrincipal
import auth.JwtProvider
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.ryanharter.ktor.moshi.moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.weesnerdevelopment.AppConfig
import com.weesnerdevelopment.DbLogger
import com.weesnerdevelopment.injecton.kodeinSetup
import com.weesnerdevelopment.routes.billManRoutes
import com.weesnerdevelopment.routes.serverRoutes
import com.weesnerdevelopment.routes.taxFetcherRoutes
import com.weesnerdevelopment.routes.userRoutes
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.jwt.jwt
import io.ktor.auth.parseAuthorizationHeader
import io.ktor.features.*
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.routing.Routing
import io.ktor.websocket.WebSockets
import kimchi.Kimchi
import logging.LoggingService
import logging.StdOutLogger
import org.kodein.di.generic.instance
import org.kodein.di.ktor.kodein
import respondErrorAuthorizing
import respondErrorServer
import shared.auth.InvalidUserReason
import java.time.Duration

object DatabaseServer {
    fun Application.main() {
        kodeinSetup()

        val appConfig by kodein().instance<AppConfig>()
        val jwtProvider by kodein().instance<JwtProvider>()
        Kimchi.addLog(StdOutLogger)

        DatabaseFactory.init()

        install(DefaultHeaders)
        if (appConfig.isDevelopment)
            install(CallLogging)
        install(WebSockets)
        install(CORS) {
            method(HttpMethod.Options)
            header(HttpHeaders.Authorization)
            host("${appConfig.baseUrl}:${appConfig.port}")
            host("localhost:3000")
            maxAge = Duration.ofDays(1)
            allowCredentials = true
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
                        else -> respondErrorServer(Throwable(e))
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
                    if (credential.payload.audience.contains(appConfig.audience)) CustomPrincipal(credential.payload)
                    else null
                }
            }
        }
        install(Routing) {
            serverRoutes()
            userRoutes()
            taxFetcherRoutes()
            billManRoutes()
        }
    }
}
