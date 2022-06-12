@file:OptIn(KtorExperimentalLocationsAPI::class)

package com.weesnerdevelopment.businessRules

import com.weesnerdevelopment.shared.base.GenericItem
import io.ktor.server.application.*
import io.ktor.server.locations.*
import io.ktor.server.locations.delete
import io.ktor.server.locations.get
import io.ktor.server.locations.post
import io.ktor.server.locations.put
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*

/**
 * Attempts to retrieve data from the user in a safe way, returning either an
 * instance of the [T] the user provided or `null` if the data could not be parsed as [T]
 */
suspend inline fun <reified T : Any> ApplicationCall.tryReceive() =
    runCatching { receiveOrNull<T>() }.getOrNull()

inline fun <reified T : Any> Route.get(
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit
): Route {
    return get<T> {
        logRequest(null)

        body(this)
    }
}

inline fun <reified T : Any, reified I : GenericItem> Route.post(
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(I?) -> Unit
): Route = post<T> {
    val item = call.tryReceive<I>()
    logRequest(item)

    body(this, item)
}

inline fun <reified T : Any, reified I : GenericItem> Route.put(
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(I?) -> Unit
): Route = put<T> {
    val item = call.tryReceive<I>()
    logRequest(item)

    body(this, item)
}

inline fun <reified T : Any> Route.delete(
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit
): Route = delete<T> {
    logRequest(null)

    body(this)
}
