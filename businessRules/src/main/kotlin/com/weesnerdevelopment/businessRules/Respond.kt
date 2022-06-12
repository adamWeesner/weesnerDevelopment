package com.weesnerdevelopment.businessRules

import com.weesnerdevelopment.shared.Paths
import com.weesnerdevelopment.shared.auth.InvalidUserException
import com.weesnerdevelopment.shared.auth.InvalidUserReason
import com.weesnerdevelopment.shared.base.GenericItem
import com.weesnerdevelopment.shared.base.HttpStatus
import com.weesnerdevelopment.shared.base.Response
import com.weesnerdevelopment.shared.base.Response.Unauthorized
import com.weesnerdevelopment.shared.base.ServerError
import com.weesnerdevelopment.shared.toJson
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*

data class CallItem(
    val instance: String,
    val time: Long = System.currentTimeMillis()
)

data class HttpLog(
    val method: String,
    val url: String,
    val statusCode: Int
) {
    constructor(method: HttpMethod, url: String, statusCode: HttpStatusCode) : this(method.value, url, statusCode.value)
}

val callItems = mutableListOf<CallItem>()

/**
 * Helper function to log request and body.
 */
fun <I : GenericItem> ApplicationCall.logRequest(body: I? = null) {
    callItems.add(CallItem(this.toString()))
    val callRequest = request.origin
    val url = "${callRequest.scheme}://${callRequest.remoteHost}:${callRequest.port}/${callRequest.uri}"
    val method = request.httpMethod.value

    Log.debug("--> $method ${callRequest.version} $url")
    if (!request.authorization().isNullOrBlank())
        Log.debug("authorization: ${request.authorization()}")
    if (body != null)
        Log.debug("body: $body")
    Log.debug("--> END $method")
}

/**
 * Helper function to log request and body.
 */
fun <I : GenericItem> PipelineContext<*, ApplicationCall>.logRequest(body: I? = null) =
    call.logRequest(body)

/**
 * helper function to log the [status] and [message] of the response along with time it took to respond.
 */
suspend fun <T : Any?> ApplicationCall.logResponse(status: HttpStatus, message: T) {
    if (!request.origin.uri.contains(Paths.BillMan.logging))
        Log.info("${HttpLog(request.httpMethod.value, request.origin.uri, status.code)}")

    respond(HttpStatusCode(status.code, status.description), message ?: "")

    val callItem = callItems.firstOrNull { it.instance == this.toString() }
    callItems.remove(callItem)

    val time = System.currentTimeMillis() - (callItem?.time ?: System.currentTimeMillis())

    Log.debug("<-- ${request.origin.version} (${time}ms)")
    Log.debug("Response: $message")
    Log.debug("<-- END HTTP (${message.toString().toByteArray().size}-byte body)")
}

/**
 * helper function to log the [status] and [message] of the response along with time it took to respond.
 */
suspend fun <T : Any?> PipelineContext<*, ApplicationCall>.logResponse(status: HttpStatus, message: T?) =
    call.logResponse(status, message)

/**
 * Helper function to [respond] with a [Response] and body.
 */
suspend fun <T : Any?> ApplicationCall.respond(response: Response<T>) {
    with(response) {
        when (this) {
            is Response.Ok,
            is Response.Created,
            is Response.NoContent ->
                logResponse(status, message)
            is Response.BadRequest,
            is Response.NotFound,
            is Response.Conflict,
            is Response.InternalError ->
                logResponse(status, ServerError(status.description, status.code, message.toString()))
            is Unauthorized ->
                logResponse(status, ServerError(status.description, status.code, reason.toJson()))
        }
    }
}

/**
 * Helper function to [respond] with a [Response] and body.
 */
suspend fun <T : Any?> PipelineContext<*, ApplicationCall>.respond(response: Response<T>) {
    call.respond(response)
}

/**
 * Helper function to [respond] with an [InvalidUserException] with the given [reason].
 */
suspend fun ApplicationCall.respondUnauthorized(reason: InvalidUserReason) =
    respond(Unauthorized(InvalidUserException(request.uri, HttpStatusCode.Unauthorized.value, reason.code)))
