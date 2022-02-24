package com.weesnerdevelopment.router

import Path
import com.weesnerdevelopment.businessRules.AppConfig
import com.weesnerdevelopment.businessRules.Log
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import kimchi.Kimchi
import logging.StdOutLogger
import org.kodein.di.generic.instance
import org.kodein.di.ktor.kodein
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
object RouterServer {
    fun Application.main() {
        initKodein()

        val appConfig by kodein().instance<AppConfig>()
//        val loggingService by kodein().instance<LoggingService>()

//        if (!appConfig.isTesting)
//            Kimchi.addLog(DbLogger.apply { service = loggingService })

        if (appConfig.isTesting || appConfig.isDevelopment)
            Kimchi.addLog(StdOutLogger)

        install(DefaultHeaders)
        if (appConfig.isDevelopment || appConfig.isTesting)
            install(CallLogging)
        install(CORS) {
            method(HttpMethod.Options)
            header(HttpHeaders.Authorization)
            host("${appConfig.baseUrl}:${appConfig.port}", schemes = listOf("http", "https"))
            host("localhost:3000")
            maxAgeDuration = Duration.days(1)
            allowCredentials = true
            allowNonSimpleContentTypes = true
        }

        val client = HttpClient(Java) {
            expectSuccess = false
        }

        install(Routing) {
            route("{...}") {
                get { call.redirectInternally(client) }
                post { call.redirectInternally(client) }
                put { call.redirectInternally(client) }
                delete { call.redirectInternally(client) }
            }
        }
    }
}

suspend fun ApplicationCall.redirectInternally(httpClient: HttpClient) {
    val cp = object : RequestConnectionPoint by request.local {
        override val scheme: String = "http"
        override val host: String = "0.0.0.0"
        override val port: Int
            get() = when {
                uri.startsWith("/${Path.User.basePath}") ->
                    if (scheme == "https") 8892 else 8082
                uri.startsWith("/${Path.BillMan.basePath}") ->
                    if (scheme == "https") 8891 else 8081
                else ->
                    8080
            }
    }
    val req = object : ApplicationRequest by request {
        override val local: RequestConnectionPoint = cp
    }
    val call = object : ApplicationCall by this {
        override val request: ApplicationRequest = req
    }

    val uriMinusParams = cp.uri.substringBefore("?")
    val url = "${cp.scheme}://${cp.host}:${cp.port}$uriMinusParams"
    val queryParams = req.queryParameters.flattenEntries()
    val requestBody = call.receiveText()

    Log.debug("attempting to make redirected call to $url")
    Log.debug("query string is $queryParams")
    Log.debug("body is $requestBody")

    val response: HttpResponse = httpClient.request(url) {
        method = cp.method
        headers { appendAll(req.headers) }
        queryParams.forEach { parameter(it.first, it.second) }
        if (requestBody.isNotBlank())
            body = requestBody
    }

    Log.debug("should have made redirected call")
    respond(response.status, response.readText())
}