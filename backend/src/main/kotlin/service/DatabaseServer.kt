package com.weesnerdevelopment.service

import auth.CustomPrincipal
import auth.JwtProvider
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.codahale.metrics.Slf4jReporter
import com.codahale.metrics.jmx.JmxReporter
import com.ryanharter.ktor.moshi.moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.weesnerdevelopment.AppConfig
import com.weesnerdevelopment.DbLogger
import com.weesnerdevelopment.injection.kodeinSetup
import com.weesnerdevelopment.routes.*
import com.weesnerdevelopment.seed.breathOfTheWildSeed
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.metrics.dropwizard.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kimchi.Kimchi
import kotlinx.coroutines.launch
import logging.LoggingService
import logging.StdOutLogger
import org.kodein.di.generic.instance
import org.kodein.di.ktor.kodein
import respondErrorAuthorizing
import respondErrorServer
import shared.auth.InvalidUserReason
import java.time.Duration
import java.util.concurrent.TimeUnit

object DatabaseServer {
    fun Application.main() {
        kodeinSetup()

        val loggingService by kodein().instance<LoggingService>()
        Kimchi.addLog(DbLogger.apply { service = loggingService })
        Kimchi.addLog(StdOutLogger)

        val appConfig by kodein().instance<AppConfig>()
        val jwtProvider by kodein().instance<JwtProvider>()

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
        install(DropwizardMetrics) {
            Slf4jReporter.forRegistry(registry)
                .outputTo(log)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build()
                .start(10, TimeUnit.SECONDS)

            JmxReporter.forRegistry(registry)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build()
                .start()
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
        install(Routing) {
            serverRoutes()
            userRoutes()
            taxFetcherRoutes()
            billManRoutes()
            breathOfTheWildRoutes()
            serialCabinetRoutes()
        }

        launch {
            breathOfTheWildSeed()
        }
    }
}
