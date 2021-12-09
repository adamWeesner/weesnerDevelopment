package com.weesnerdevelopment.businessRules

import HttpLog
import Path
import callItems
import com.weesnerdevelopment.shared.base.GenericItem
import com.weesnerdevelopment.shared.base.Response
import com.weesnerdevelopment.shared.toJson
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.locations.delete
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.locations.put
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import kimchi.Kimchi
import logRequest

/**
 * Attempts to retrieve data from the user in a safe way, returning either an
 * instance of the [T] the user provided or `null` if the data could not be parsed as [T]
 */
suspend inline fun <reified T : Any> ApplicationCall.tryReceive() =
    runCatching { receiveOrNull<T>() }.getOrNull()

@OptIn(KtorExperimentalLocationsAPI::class)
inline fun <reified T : Any> Route.get(
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit
): Route = get<T> {
    logRequest(null)

    body(this)
}

@OptIn(KtorExperimentalLocationsAPI::class)
inline fun <reified T : Any, reified I : GenericItem> Route.post(
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(I?) -> Unit
): Route = post<T> {
    val item = call.tryReceive<I>()
    logRequest(item)

    body(this, item)
}

@OptIn(KtorExperimentalLocationsAPI::class)
inline fun <reified T : Any, reified I : GenericItem> Route.put(
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(I?) -> Unit
): Route = put<T> {
    val item = call.tryReceive<I>()
    logRequest(item)

    body(this, item)
}

@OptIn(KtorExperimentalLocationsAPI::class)
inline fun <reified T : Any> Route.delete(
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit
): Route = delete<T> {
    logRequest(null)

    body(this)
}

/**
 * Helper function to [respond] with a [Response] and body.
 */
suspend inline fun <reified T : Any> PipelineContext<*, ApplicationCall>.respond(status: HttpStatusCode, response: T) =
    response.run {
        if (!call.request.origin.uri.contains(Path.BillMan.logging))
            Kimchi.info("${HttpLog(call.request.httpMethod.value, call.request.origin.uri, status.value)}")

        call.respond(status, response).also {
            val callItem = callItems.firstOrNull { it.instance == this@respond.toString() }
            callItems.remove(callItem)

            val parsedResponse = when (response) {
                is String, is Int, is Long, is Double, is Boolean -> response.toString()
                else -> response.toJson()
            }

            val time = System.currentTimeMillis() - (callItem?.time ?: System.currentTimeMillis())

            Kimchi.debug("<-- ${call.request.origin.version} ${status.value} ${status.description} (${time}ms)")
            Kimchi.debug("Response: $parsedResponse")
            Kimchi.debug("<-- END HTTP (${parsedResponse.toByteArray().size}-byte body)")
        }
    }