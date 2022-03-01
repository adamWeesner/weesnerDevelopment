package com.weesnerdevelopment.router

import com.weesnerdevelopment.businessRules.AppConfig
import com.weesnerdevelopment.businessRules.Log
import com.weesnerdevelopment.shared.Paths
import com.weesnerdevelopment.shared.base.Response.Companion.BadRequest
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
import io.ktor.serialization.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import kimchi.Kimchi
import kotlinx.serialization.ExperimentalSerializationApi
import logging.StdOutLogger
import org.kodein.di.generic.instance
import org.kodein.di.ktor.kodein
import respondError
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalSerializationApi::class, ExperimentalTime::class)
object RouterServer {
    fun Application.main() {
        initKodein()

        val appConfig by kodein().instance<AppConfig>()
//        val loggingService by kodein().instance<LoggingService>()

//        if (!appConfig.isTesting)
//            Kimchi.addLog(DbLogger.apply { service = loggingService })

        Kimchi.addLog(StdOutLogger)

        install(StatusPages) {
            exception<java.net.ConnectException> { cause ->
                val data = getRequestDataFromCall()

                Log.warn("A java connect exception happened\n$data\n", cause)
                respondError(BadRequest("An error occurred trying to parse your request"))
            }
            exception<io.ktor.http.UnsafeHeaderException> { cause ->
                val data = getRequestDataFromCall()

                Log.warn("A unsafe header exception happened\n$data\n", cause)
                respondError(BadRequest("An error occurred trying to parse your request"))
            }
            exception<java.lang.IllegalArgumentException> { cause ->
                val data = getRequestDataFromCall()

                Log.warn("A illegal argument exception happened\n$data\n", cause)
                respondError(BadRequest("An error occurred trying to parse your request"))
            }
        }

        install(DefaultHeaders) {
            header(HttpHeaders.AcceptCharset, Charsets.UTF_8.toString())
            header(
                HttpHeaders.Accept,
                ContentType.Application.Json.withParameter("charset", Charsets.UTF_8.toString()).toString()
            )
        }
        if (appConfig.isDevelopment || appConfig.isTesting)
            install(CallLogging)
        install(HSTS)
        install(CORS) {
            method(HttpMethod.Options)
            header(HttpHeaders.ContentType)
            header(HttpHeaders.Authorization)
            host("${appConfig.baseUrl}:${appConfig.sslPort}", schemes = listOf("https"))
            host(appConfig.baseUrl, schemes = listOf("https"))
            if (appConfig.isTesting || appConfig.isDevelopment) {
                host("${appConfig.baseUrl}:${appConfig.port}", schemes = listOf("http"))
                host("localhost:3000")
            }
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

        val client = HttpClient(Java) {
            expectSuccess = false
        }

        install(Routing) {
            route("{...}") {
                get { redirectInternally(client) }
                post { redirectInternally(client) }
                put { redirectInternally(client) }
                delete { redirectInternally(client) }
            }
        }
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.getRequestDataFromCall(): RequestData {
    val cp = object : RequestConnectionPoint by call.request.local {}

    val version = cp.version
    val uriMinusParams = cp.uri.substringBefore("?")
    val url = "${cp.scheme}://${cp.host}:${cp.port}$uriMinusParams"
    val queryParams = call.request.queryParameters.flattenEntries()
    val requestBody = runCatching { call.receiveText() }.getOrNull()

    return RequestData(
        version = version,
        url = url,
        queryParams = queryParams,
        headers = call.request.headers.flattenEntries(),
        body = requestBody ?: "Body could not be retrieved"
    )
}

data class RequestData(
    val version: String,
    val url: String,
    val queryParams: List<Pair<String, String>>,
    val headers: List<Pair<String, String>>,
    val body: String
) {
    override fun toString(): String {
        return "" +
                " version: $version\n" +
                "     url: $url\n" +
                "  params: $queryParams\n" +
                " headers: ${headers}\n" +
                "    body: $body"
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.redirectInternally(httpClient: HttpClient) {
    val cp = object : RequestConnectionPoint by call.request.local {
        override val port: Int
            get() = when {
                uri.startsWith("/${Paths.User.basePath}", true) ->
                    8082
                uri.startsWith("/${Paths.BillMan.basePath}", true) ->
                    8081
                else ->
                    call.request.local.port
            }
    }
    val req = object : ApplicationRequest by call.request {
        override val local: RequestConnectionPoint = cp
    }
    val call = object : ApplicationCall by call {
        override val request: ApplicationRequest = req
    }

    val requestData = getRequestDataFromCall()


    if (!cp.host.startsWith("api", true) || cp.port == 8080 || cp.port == 8443) {
        Log.warn("Tried to call router server:\n$requestData")
        respondError(BadRequest("An error occurred trying to parse your request"))
        return
    }

    val uriMinusParams = cp.uri.substringBefore("?")
    val internalUrl = "http://0.0.0.0:${cp.port}$uriMinusParams"

    Log.debug(
        "attempting to make redirected call from:\n" +
                "$requestData\n" +
                "-------- to --------\n" +
                " version: ${requestData.version}\n" +
                "     url: $internalUrl\n" +
                "  params: ${requestData.queryParams}\n" +
                " headers: ${requestData.headers}\n" +
                "    body: ${requestData.body}"
    )

    val response: HttpResponse = httpClient.request(internalUrl) {
        method = cp.method
        headers { appendAll(req.headers) }
        requestData.queryParams.forEach { parameter(it.first, it.second) }
        if (requestData.body.isNotBlank())
            body = requestData.body
    }

    call.respond(response.status, response.readText())
}