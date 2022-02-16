package com.weesnerdevelopment.billman.bill.occurrence

import com.weesnerdevelopment.businessRules.*
import com.weesnerdevelopment.businessRules.get
import com.weesnerdevelopment.shared.base.ServerError
import com.weesnerdevelopment.shared.billMan.BillOccurrence
import com.weesnerdevelopment.shared.billMan.responses.BillOccurrencesResponse
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.locations.delete
import io.ktor.routing.*
import logRequest
import java.util.*
import io.ktor.locations.put as locationPut

@OptIn(KtorExperimentalLocationsAPI::class)
data class BillOccurrenceRouterImpl(
    val repo: BillOccurrenceRepository
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
            authenticate {
                get<BillOccurrenceEndpoint> {
                    val id = call.occurrenceId
                    val userUuid = getBearerUuid().toString()

                    if (id.isNullOrBlank()) {
                        val occurrences = repo.getAll(userUuid)
                        return@get respond(HttpStatusCode.OK, BillOccurrencesResponse(occurrences))
                    }

                    val isValidUuid = runCatching { UUID.fromString(id) }.getOrNull()

                    if (isValidUuid == null) {
                        return@get respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Invalid id '$id' attempting to get bill occurrence."
                            )
                        )
                    }

                    return@get when (val foundBillOccurrence = repo.get(userUuid, id)) {
                        null -> respond(
                            HttpStatusCode.NotFound,
                            ServerError(
                                HttpStatusCode.NotFound.description,
                                HttpStatusCode.NotFound.value,
                                "No bill occurrence with id '$id' found."
                            )
                        )
                        else -> respond(HttpStatusCode.OK, foundBillOccurrence)
                    }
                }

                post<BillOccurrenceEndpoint, BillOccurrence> { billOccurrence ->
                    val userUuid = getBearerUuid().toString()

                    if (billOccurrence == null)
                        return@post respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Cannot add invalid bill occurrence."
                            )
                        )

                    if (billOccurrence.owner != userUuid) {
                        Log.warn("The owner of the bill occurrence attempting to add and the bearer token did not match. Bearer id $userUuid bill occurrence $billOccurrence")
                        return@post respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Cannot add bill occurrence."
                            )
                        )
                    }

                    val newBillOccurrence = repo.add(billOccurrence)
                    if (newBillOccurrence == null) {
                        return@post respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "An error occurred attempting to add bill occurrence."
                            )
                        )
                    }

                    return@post respond(HttpStatusCode.Created, newBillOccurrence)
                }

                locationPut<BillOccurrencePayEndpoint> {
                    logRequest(null)

                    val userUuid = getBearerUuid().toString()

                    val id = call.occurrenceId
                    val paymentAmount = call.payment

                    if (id == null || paymentAmount == null) {
                        return@locationPut respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Cannot pay for a bill occurrence with invalid id or paymentAmount."
                            )
                        )
                    }

                    val foundOccurrence = repo.get(userUuid, id)

                    if (foundOccurrence == null) {
                        return@locationPut respond(
                            HttpStatusCode.NotFound,
                            ServerError(
                                HttpStatusCode.NotFound.description,
                                HttpStatusCode.NotFound.value,
                                "No bill occurrence with id '$id' found."
                            )
                        )
                    }

                    if (foundOccurrence.sharedUsers?.contains(userUuid) == false) {
                        Log.warn("The owner of the bill occurrence attempting to update and the bearer token did not match. Bearer id $userUuid bill occurrence $foundOccurrence")
                        return@locationPut respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Cannot update bill occurrence."
                            )
                        )
                    }

                    val payment = repo.pay(id, paymentAmount)
                    if (payment == null) {
                        return@locationPut respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "An error occurred attempting to pay for bill occurrence."
                            )
                        )
                    }

                    return@locationPut respond(HttpStatusCode.OK, payment)
                }

                put<BillOccurrenceEndpoint, BillOccurrence> { billOccurrence ->
                    val userUuid = getBearerUuid().toString()

                    if (billOccurrence == null) {
                        return@put respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Cannot update invalid bill occurrence."
                            )
                        )
                    }

                    if (billOccurrence.sharedUsers?.contains(userUuid) == false) {
                        Log.warn("The owner of the bill occurrence attempting to update and the bearer token did not match. Bearer id $userUuid bill occurrence $billOccurrence")
                        return@put respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Cannot update bill occurrence."
                            )
                        )
                    }

                    val updatedBillOccurrence = repo.update(billOccurrence)
                    if (updatedBillOccurrence == null) {
                        return@put respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "An error occurred attempting to update bill occurrence."
                            )
                        )
                    }

                    return@put respond(HttpStatusCode.OK, updatedBillOccurrence)
                }

                delete<BillOccurrenceEndpoint> {
                    val id = call.occurrenceId
                    val authUuid = getBearerUuid().toString()

                    if (id.isNullOrBlank() || runCatching { UUID.fromString(id) }.getOrNull() == null) {
                        return@delete respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Invalid id '$id' attempting to delete bill occurrence."
                            )
                        )
                    }

                    when (val deletedBillOccurrence = repo.delete(authUuid, id)) {
                        false -> return@delete respond(
                            HttpStatusCode.NotFound,
                            ServerError(
                                HttpStatusCode.NotFound.description,
                                HttpStatusCode.NotFound.value,
                                "No bill occurrence with id '$id' found."
                            )
                        )
                        else -> return@delete respond(HttpStatusCode.OK, deletedBillOccurrence)
                    }
                }
            }
        }
    }
}