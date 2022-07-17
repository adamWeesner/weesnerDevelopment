package com.weesnerdevelopment.billman.bill.occurrence

import auth.AuthValidator
import com.weesnerdevelopment.businessRules.*
import com.weesnerdevelopment.businessRules.get
import com.weesnerdevelopment.shared.base.Response
import com.weesnerdevelopment.shared.billMan.BillOccurrence
import com.weesnerdevelopment.shared.billMan.responses.BillOccurrencesResponse
import io.ktor.server.application.*
import io.ktor.server.locations.*
import io.ktor.server.locations.delete
import io.ktor.server.routing.*
import java.util.*
import io.ktor.server.locations.put as locationPut

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

    override fun setup(route: Route) {
        route.apply {
            get<BillOccurrenceEndpoint> {
                val id = call.occurrenceId
                val userUuid = authValidator.getUuid(this)

                if (id.isNullOrBlank()) {
                    val occurrences = repo.getAll(userUuid)
                    return@get respond(Response.Ok(BillOccurrencesResponse(occurrences)))
                }

                if (runCatching { UUID.fromString(id) }.getOrNull() == null)
                    return@get respond(Response.BadRequest("Invalid id '$id' attempting to get bill occurrence."))

                return@get when (val foundBillOccurrence = repo.get(userUuid, id)) {
                    null -> respond(Response.NotFound("No bill occurrence with id '$id' found."))
                    else -> respond(Response.Ok(foundBillOccurrence))
                }
            }

            post<BillOccurrenceEndpoint, BillOccurrence> { billOccurrence ->
                val userUuid = authValidator.getUuid(this)

                if (billOccurrence == null)
                    return@post respond(Response.BadRequest("Cannot add invalid bill occurrence."))

                if (billOccurrence.owner != userUuid) {
                    Log.warn("The owner of the bill occurrence attempting to add and the bearer token did not match. Bearer id $userUuid bill occurrence $billOccurrence")
                    return@post respond(Response.BadRequest("Cannot add bill occurrence."))
                }

                return@post when (val newBillOccurrence = repo.add(billOccurrence)) {
                    null -> respond(Response.BadRequest("An error occurred attempting to add bill occurrence."))
                    else -> respond(Response.Created(newBillOccurrence))
                }
            }

            locationPut<BillOccurrencePayEndpoint> {
                logRequest(null)

                val userUuid = authValidator.getUuid(this)

                val id = call.occurrenceId
                val paymentAmount = call.payment

                if (id == null || paymentAmount == null || runCatching { UUID.fromString(id) }.getOrNull() == null)
                    return@locationPut respond(Response.BadRequest("Cannot pay for a bill occurrence with invalid id or paymentAmount."))

                val foundOccurrence = repo.get(userUuid, id)

                if (foundOccurrence == null)
                    return@locationPut respond(Response.NotFound("No bill occurrence with id '$id' found."))

                if (foundOccurrence.sharedUsers?.contains(userUuid) == false) {
                    Log.warn("The owner of the bill occurrence attempting to update and the bearer token did not match. Bearer id $userUuid bill occurrence $foundOccurrence")
                    return@locationPut respond(Response.BadRequest("Cannot update bill occurrence."))
                }

                return@locationPut when (val payment = repo.pay(id, paymentAmount)) {
                    null -> respond(Response.BadRequest("An error occurred attempting to pay for bill occurrence."))
                    else -> respond(Response.Ok(payment))
                }
            }

            put<BillOccurrenceEndpoint, BillOccurrence> { billOccurrence ->
                val userUuid = authValidator.getUuid(this)

                if (billOccurrence == null)
                    return@put respond(Response.BadRequest("Cannot update invalid bill occurrence."))

                if (billOccurrence.sharedUsers?.contains(userUuid) == false) {
                    Log.warn("The owner of the bill occurrence attempting to update and the bearer token did not match. Bearer id $userUuid bill occurrence $billOccurrence")
                    return@put respond(Response.BadRequest("Cannot update bill occurrence."))
                }

                return@put when (val updatedBillOccurrence = repo.update(billOccurrence)) {
                    null -> respond(Response.BadRequest("An error occurred attempting to update bill occurrence."))
                    else -> respond(Response.Ok(updatedBillOccurrence))
                }
            }

            delete<BillOccurrenceEndpoint> {
                val id = call.occurrenceId
                val authUuid = authValidator.getUuid(this)

                if (id.isNullOrBlank() || runCatching { UUID.fromString(id) }.getOrNull() == null)
                    return@delete respond(Response.BadRequest("Invalid id '$id' attempting to delete bill occurrence."))

                return@delete when (val deletedBillOccurrence = repo.delete(authUuid, id)) {
                    false -> respond(Response.NotFound("No bill occurrence with id '$id' found."))
                    else -> respond(Response.Ok(deletedBillOccurrence))
                }
            }
        }
    }
}