package com.weesnerdevelopment.billman

import Path.BillMan.health
import auth.CustomPrincipal
import auth.JwtProvider
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.codahale.metrics.jmx.JmxReporter
import com.weesnerdevelopment.businessRules.AppConfig
import com.weesnerdevelopment.shared.auth.InvalidUserReason
import com.weesnerdevelopment.shared.base.Response
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.locations.*
import io.ktor.metrics.dropwizard.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.websocket.*
import kimchi.Kimchi
import kotlinx.serialization.ExperimentalSerializationApi
import logging.StdOutLogger
import org.kodein.di.generic.instance
import org.kodein.di.ktor.kodein
import respond
import respondErrorAuthorizing
import respondErrorServer
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class, ExperimentalSerializationApi::class)
object BillManServer {
    fun Application.main() {
        initKodein()

        val appConfig by kodein().instance<AppConfig>()
        val jwtProvider by kodein().instance<JwtProvider>()
//        val loggingService by kodein().instance<LoggingService>()

//        if (!appConfig.isTesting)
//            Kimchi.addLog(DbLogger.apply { service = loggingService })

        if (appConfig.isTesting || appConfig.isDevelopment)
            Kimchi.addLog(StdOutLogger)

        BillManDatabase.init(appConfig.isTesting)

        install(DefaultHeaders)
        if (appConfig.isDevelopment || appConfig.isTesting)
            install(CallLogging)
        install(WebSockets)
        install(CORS) {
            method(HttpMethod.Options)
            header(HttpHeaders.Authorization)
            host("${appConfig.baseUrl}:${appConfig.port}")
            host("localhost:3000")
            maxAgeDuration = Duration.days(1)
            allowCredentials = true
            allowNonSimpleContentTypes = true
        }
        if (!appConfig.isTesting) {
            install(DropwizardMetrics) {
//            Slf4jReporter.forRegistry(registry)
//                .outputTo(log)
//                .convertRatesTo(TimeUnit.SECONDS)
//                .convertDurationsTo(TimeUnit.MILLISECONDS)
//                .build()
//                .start(10, TimeUnit.SECONDS)

                JmxReporter.forRegistry(registry)
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .build()
                    .start()
            }
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
                    if (credential.payload.audience.contains(appConfig.audience)) CustomPrincipal(credential.payload)
                    else null
                }
            }
        }
        install(Locations)
        install(Routing) {
            route(health) {
                get {
                    respond(Response.Ok("Server is up and running"))
                }
            }

            routes()
        }
    }
}