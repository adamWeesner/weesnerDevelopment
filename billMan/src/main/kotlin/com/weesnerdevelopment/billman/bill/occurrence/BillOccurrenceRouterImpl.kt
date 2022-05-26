package com.weesnerdevelopment.billman.bill.occurrence

import auth.AuthValidator
import com.weesnerdevelopment.businessRules.*
import com.weesnerdevelopment.businessRules.get
import com.weesnerdevelopment.shared.billMan.BillOccurrence
import com.weesnerdevelopment.shared.billMan.responses.BillOccurrencesResponse
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.locations.delete
import io.ktor.routing.*
import logRequest
import java.util.*
import io.ktor.locations.put as locationPut

@OptIn(KtorExperimentalLocationsAPI::class)
data class BillOccurrenceRouterImpl(
    val repo: BillOccurrenceRepository,
    val authValidator: AuthValidator
) : BillOccurrenceRouter {
    /**
     * Reduces typing to get the param for `?id=` :)
     */
    private val ApplicationCall.occurrenceId
        get() = request.queryParameters[BillOccurrenceEndpoint::id.name]

    private val ApplicationCall.payment
        get() = request.queryParameters[BillOccurrencePayEndpoint::payment.name]

    override fun setup(routing: Routing) {
        routing.apply {
            get<BillOccurrenceEndpoint> {
                val id = call.occurrenceId
                val userUuid = authValidator.getUuid(this)

                if (id.isNullOrBlank()) {
                    val occurrences = repo.getAll(userUuid)
                    return@get respond(HttpStatusCode.OK, BillOccurrencesResponse(occurrences))
                }

                if (runCatching { UUID.fromString(id) }.getOrNull() == null)
                    return@get respondWithError(
                        HttpStatusCode.BadRequest,
                        "Invalid id '$id' attempting to get bill occurrence."
                    )

                return@get when (val foundBillOccurrence = repo.get(userUuid, id)) {
                    null -> respondWithError(HttpStatusCode.NotFound, "No bill occurrence with id '$id' found.")
                    else -> respond(HttpStatusCode.OK, foundBillOccurrence)
                }
            }

            post<BillOccurrenceEndpoint, BillOccurrence> { billOccurrence ->
                val userUuid = authValidator.getUuid(this)

                if (billOccurrence == null)
                    return@post respondWithError(HttpStatusCode.BadRequest, "Cannot add invalid bill occurrence.")

                if (billOccurrence.owner != userUuid) {
                    Log.warn("The owner of the bill occurrence attempting to add and the bearer token did not match. Bearer id $userUuid bill occurrence $billOccurrence")
                    return@post respondWithError(HttpStatusCode.BadRequest, "Cannot add bill occurrence.")
                }

                return@post when (val newBillOccurrence = repo.add(billOccurrence)) {
                    null -> respondWithError(
                        HttpStatusCode.BadRequest,
                        "An error occurred attempting to add bill occurrence."
                    )
                    else -> respond(HttpStatusCode.Created, newBillOccurrence)
                }
            }

            locationPut<BillOccurrencePayEndpoint> {
                logRequest(null)

                val userUuid = authValidator.getUuid(this)

                val id = call.occurrenceId
                val paymentAmount = call.payment

                if (id == null || paymentAmount == null || runCatching { UUID.fromString(id) }.getOrNull() == null)
                    return@locationPut respondWithError(
                        HttpStatusCode.BadRequest,
                        "Cannot pay for a bill occurrence with invalid id or paymentAmount."
                    )

                val foundOccurrence = repo.get(userUuid, id)

                if (foundOccurrence == null)
                    return@locationPut respondWithError(
                        HttpStatusCode.NotFound,
                        "No bill occurrence with id '$id' found."
                    )

                if (foundOccurrence.sharedUsers?.contains(userUuid) == false) {
                    Log.warn("The owner of the bill occurrence attempting to update and the bearer token did not match. Bearer id $userUuid bill occurrence $foundOccurrence")
                    return@locationPut respondWithError(HttpStatusCode.BadRequest, "Cannot update bill occurrence.")
                }

                return@locationPut when (val payment = repo.pay(id, paymentAmount)) {
                    null -> respondWithError(
                        HttpStatusCode.BadRequest,
                        "An error occurred attempting to pay for bill occurrence."
                    )
                    else -> respond(HttpStatusCode.OK, payment)
                }
            }

            put<BillOccurrenceEndpoint, BillOccurrence> { billOccurrence ->
                val userUuid = authValidator.getUuid(this)

                if (billOccurrence == null)
                    return@put respondWithError(HttpStatusCode.BadRequest, "Cannot update invalid bill occurrence.")

                if (billOccurrence.sharedUsers?.contains(userUuid) == false) {
                    Log.warn("The owner of the bill occurrence attempting to update and the bearer token did not match. Bearer id $userUuid bill occurrence $billOccurrence")
                    return@put respondWithError(HttpStatusCode.BadRequest, "Cannot update bill occurrence.")
                }

                return@put when (val updatedBillOccurrence = repo.update(billOccurrence)) {
                    null -> respondWithError(
                        HttpStatusCode.BadRequest,
                        "An error occurred attempting to update bill occurrence."
                    )
                    else -> respond(HttpStatusCode.OK, updatedBillOccurrence)
                }
            }

            delete<BillOccurrenceEndpoint> {
                val id = call.occurrenceId
                val authUuid = authValidator.getUuid(this)

                if (id.isNullOrBlank() || runCatching { UUID.fromString(id) }.getOrNull() == null)
                    return@delete respondWithError(
                        HttpStatusCode.BadRequest,
                        "Invalid id '$id' attempting to delete bill occurrence."
                    )

                return@delete when (val deletedBillOccurrence = repo.delete(authUuid, id)) {
                    false -> respondWithError(HttpStatusCode.NotFound, "No bill occurrence with id '$id' found.")
                    else -> respond(HttpStatusCode.OK, deletedBillOccurrence)
                }
            }
        }
    }
}