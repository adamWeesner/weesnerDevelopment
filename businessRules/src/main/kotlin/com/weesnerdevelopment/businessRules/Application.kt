package com.weesnerdevelopment.businessRules

import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.weesnerdevelopment.businessRules.auth.AuthProvider
import com.weesnerdevelopment.shared.auth.InvalidUserReason
import com.weesnerdevelopment.shared.base.Response
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.*
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonBuilder
import org.slf4j.event.Level
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

/**
 * Installs [CallLogging] with default logging format. Can filter what log level threshold to have with [level].
 */
fun Application.installCallLogging(level: Level) {
    install(CallLogging) {
        this.level = level

        format { call ->
            val responseStatus = call.response.status()
            val statusInfo = "${responseStatus?.value} ${responseStatus?.description}:"
            val uriInfo = "${call.request.httpMethod.value} - ${call.request.uri}"
            val info = "$statusInfo $uriInfo"

            Log.info(info)
            info
        }
    }
}

/**
 * Installs [DefaultHeaders] with the default headers applications generally have. You can use [additionalHeaders] to
 * add custom headers as you see fit.
 */
fun Application.installDefaultHeaders(additionalHeaders: DefaultHeadersConfig.() -> Unit = {}) {
    install(DefaultHeaders) {
        header(HttpHeaders.AcceptCharset, Charsets.UTF_8.toString())
        header(
            HttpHeaders.Accept,
            ContentType.Application.Json.withParameter("charset", Charsets.UTF_8.toString()).toString()
        )
        additionalHeaders()
    }
}

/**
 * Installs [ContentNegotiation] using [Serializable] with sane defaults. You can add custom rules with the
 * [customJsonRules] lambda
 */
@OptIn(ExperimentalSerializationApi::class)
fun Application.installContentNegotiation(customJsonRules: JsonBuilder.() -> Unit = {}) {
    install(ContentNegotiation) {
        json(com.weesnerdevelopment.shared.json {
            prettyPrint = true
            prettyPrintIndent = "  "
            isLenient = true
            customJsonRules()
        })
    }
}

/**
 * Installs [Authentication] using the [authProvider] to determine the authentication method to use.
 */
fun Application.installAuthentication(authProvider: AuthProvider) {
    install(Authentication) {
        authProvider.configure(this)
    }
}

/**
 * Installs [StatusPages] with sane defaults.
 */
fun Application.installStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, e ->
            with(call) {
                when (e) {
                    is TokenExpiredException -> respondUnauthorized(InvalidUserReason.Expired)
                    is JWTVerificationException -> respondUnauthorized(InvalidUserReason.InvalidJwt)
                    else -> respond(Response.BadRequest(e.message ?: "An error occurred ${e::class}"))
                }
            }
        }
    }
}

/**
 * Installs [CORS] with sane defaults, you need to add the appropriate [CORSHosts] for the given api.
 */
@OptIn(ExperimentalTime::class)
fun Application.installCORS(vararg CORSHosts: CORSHost) {
    install(CORS) {
        methods.add(HttpMethod.Options)
        headers.apply {
            add(HttpHeaders.ContentType)
            add(HttpHeaders.Authorization)
        }
        CORSHosts.forEach {
            allowHost(it.host, schemes = listOf(it.scheme.value))
        }
        maxAgeDuration = Duration.days(1)
        allowCredentials = true
        allowNonSimpleContentTypes = true
    }
}

enum class HttpScheme(val value: String) { Http("http"), Https("https") }
data class CORSHost(val host: String, val scheme: HttpScheme)