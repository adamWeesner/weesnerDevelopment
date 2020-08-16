package occurrences

import BaseRouter
import io.ktor.application.call
import io.ktor.request.receiveOrNull
import io.ktor.routing.Route
import io.ktor.routing.put
import parse
import respond
import respondError
import respondErrorAuthorizing
import shared.auth.InvalidUserReason
import shared.auth.User
import shared.base.Response.Companion.BadRequest
import shared.base.Response.Companion.NotFound
import shared.base.Response.Companion.Ok
import shared.billMan.BillOccurrence
import shared.billMan.responses.BillOccurrencesResponse
import tokenAsUser
import kotlin.reflect.full.createType

class BillOccurrenceRouter(
    override val basePath: String,
    service: BillOccurrencesService
) : BaseRouter<BillOccurrence, BillOccurrencesService>(
    BillOccurrencesResponse(),
    service,
    BillOccurrence::class.createType()
) {
    override fun Route.updateRequest() {
        put {
            val body = runCatching {
                call.receiveOrNull<BillOccurrence>(kType)
            }.getOrNull()

            if (body != null) {
                val response = if (body.id == null) {
                    BadRequest("Cannot update item without id")
                } else {
                    when (val updatedItem = service.update(body) {
                        service.table.id eq body.id!!
                    }) {
                        null -> BadRequest("Failed to update $body.")
                        else -> Ok("Updated item to database with id $updatedItem")
                    }
                }

                return@put call.respond(response)
            }

            val user = tokenAsUser(service.usersService)?.redacted()?.parse<User>()
                ?: return@put call.respondErrorAuthorizing(InvalidUserReason.NoUserFound)

            val occurrenceId = call.request.queryParameters["id"]
                ?: return@put call.respondError(BadRequest("Invalid occurrence id."))

            val payment = call.request.queryParameters["pay"]?.toDoubleOrNull()
                ?: return@put call.respondError(BadRequest("Invalid amount."))

            val occurrence = service.get {
                service.table.id eq occurrenceId.toInt()
            } ?: return@put call.respondError(NotFound("No occurrence found for id $occurrenceId."))

            val newPayments = service.run {
                occurrenceId.toInt().payFor(payment, user)
            }

            val updatedOccurrence = occurrence.copy(
                amountLeft = occurrence.amountLeft.toDouble().minus(payment).toString(),
                payments = newPayments
            )

            val response =
                when (val updated = service.update(updatedOccurrence) {
                    service.table.id eq updatedOccurrence.id!!
                }) {
                    null -> BadRequest("Failed to complete payment.")
                    else -> Ok("Updated item to database with id $updated")
                }

            call.respond(response)
        }
    }
}
