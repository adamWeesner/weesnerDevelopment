package com.weesnerdevelopment.billman.bill.occurrence

import com.weesnerdevelopment.auth.user.getBearerUuid
import com.weesnerdevelopment.businessRules.get
import com.weesnerdevelopment.businessRules.post
import com.weesnerdevelopment.businessRules.put
import com.weesnerdevelopment.businessRules.respond
import com.weesnerdevelopment.shared.base.ServerError
import com.weesnerdevelopment.shared.billMan.BillOccurrence
import com.weesnerdevelopment.shared.billMan.responses.BillOccurrencesResponse
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.locations.*
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
                    val userUuid = getBearerUuid()!!

                    if (id.isNullOrBlank()) {
                        val occurrences = repo.getAll(userUuid)
                        return@get respond(HttpStatusCode.OK, BillOccurrencesResponse(occurrences))
                    }

                    val idAsUuid = runCatching { UUID.fromString(id) }.getOrNull()

                    if (idAsUuid == null)
                        return@get respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Invalid id '$id' attempting to get income occurrence."
                            )
                        )

                    return@get when (val foundBillOccurrence = repo.get(userUuid, idAsUuid)) {
                        null -> respond(
                            HttpStatusCode.NotFound,
                            ServerError(
                                HttpStatusCode.NotFound.description,
                                HttpStatusCode.NotFound.value,
                                "No income occurrence with id '$idAsUuid' found."
                            )
                        )
                        else -> respond(HttpStatusCode.OK, foundBillOccurrence)
                    }
                }

                post<BillOccurrenceEndpoint, BillOccurrence> { billOccurrence ->
                    if (billOccurrence == null)
                        return@post respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Cannot add invalid income occurrence."
                            )
                        )

                    val newBillOccurrence = repo.add(billOccurrence)
                    if (newBillOccurrence == null)
                        return@post respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "An error occurred attempting to add income occurrence."
                            )
                        )

                    return@post respond(HttpStatusCode.Created, newBillOccurrence)
                }

                locationPut<BillOccurrencePayEndpoint> {
                    val id = call.occurrenceId
                    val paymentAmount = call.payment

                    logRequest(null)
                    // todo finish this...
                }

                put<BillOccurrenceEndpoint, BillOccurrence> { billOccurrence ->
                    if (billOccurrence == null)
                        return@put respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Cannot update invalid income occurrence."
                            )
                        )

                    val updatedBillOccurrence = repo.update(billOccurrence)
                    if (updatedBillOccurrence == null)
                        return@put respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "An error occurred attempting to update income occurrence."
                            )
                        )

                    return@put respond(HttpStatusCode.Created, updatedBillOccurrence)
                }

                delete<BillOccurrenceEndpoint> {
                    val id = call.occurrenceId
                    val authUuid = getBearerUuid()!!

                    if (id.isNullOrBlank()) {
                        return@delete respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Invalid id '$id' attempting to delete income occurrence."
                            )
                        )
                    }

                    val idAsUuid = runCatching { UUID.fromString(id) }.getOrNull()

                    if (idAsUuid == null)
                        return@delete respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Invalid id '$id' attempting to delete income occurrence."
                            )
                        )

                    when (val deletedBillOccurrence = repo.delete(authUuid, idAsUuid)) {
                        false -> return@delete respond(
                            HttpStatusCode.NotFound,
                            ServerError(
                                HttpStatusCode.NotFound.description,
                                HttpStatusCode.NotFound.value,
                                "No income occurrence with id '$idAsUuid' found."
                            )
                        )
                        else -> return@delete respond(HttpStatusCode.OK, deletedBillOccurrence)
                    }
                }
            }
        }
    }
}