package com.weesnerdevelopment

import com.weesnerdevelopment.Path.TaxFetcher.basePath
import com.weesnerdevelopment.Path.User.basePath
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.request.uri
import io.ktor.response.respond
import toJson

/**
 * The available paths at /[Path].
 */
sealed class Path {
    /**
     * The available paths at [basePath]/value.
     */
    object TaxFetcher : Path() {
        private val basePath = "taxFetcher/"
        val socialSecurity = "${basePath}socialSecurity"
        val medicare = "${basePath}medicare"
        val federalIncomeTax = "${basePath}federalIncomeTax"
        val taxWithholding = "${basePath}taxWithholding"
    }

    /**
     * The available paths at [basePath]/value.
     */
    object User : Path() {
        private val basePath = "user/"
        val me = "${basePath}me"
    }
}

/**
 * Server Error generating a nice looking json error when there is a server issue.
 */
data class ServerError(
    val status: String,
    val statusCode: Int,
    val message: String
)

/**
 * Helper function to [respond] with a [ServerError].
 */
internal suspend fun ApplicationCall.respondServerError(error: Throwable) {
    val httpStatus = HttpStatusCode.InternalServerError
    respond(
        httpStatus,
        ServerError(
            httpStatus.description,
            httpStatus.value,
            error.localizedMessage ?: error.message ?: error.toString()
        )
    )
}

/**
 * Helper function to [respond] with an [InvalidUserException] with the given [reason].
 */
internal suspend fun ApplicationCall.respondAuthorizationIssue(reason: InvalidUserReason) {
    val httpStatus = HttpStatusCode.Unauthorized
    respond(httpStatus, InvalidUserException(request.uri, httpStatus.value, reason.code).toJson())
}
