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
import org.slf4j.event.Level
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

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

fun Application.installDefaultHeaders() {
    install(DefaultHeaders) {
        header(HttpHeaders.AcceptCharset, Charsets.UTF_8.toString())
        header(
            HttpHeaders.Accept,
            ContentType.Application.Json.withParameter("charset", Charsets.UTF_8.toString()).toString()
        )
    }
}

@OptIn(ExperimentalSerializationApi::class)
fun Application.installContentNegotiation() {
    install(ContentNegotiation) {
        json(com.weesnerdevelopment.shared.json {
            prettyPrint = true
            prettyPrintIndent = "  "
            isLenient = true
        })
    }
}

fun Application.installAuthentication(authProvider: AuthProvider) {
    install(Authentication) {
        authProvider.configure(this)
    }
}

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