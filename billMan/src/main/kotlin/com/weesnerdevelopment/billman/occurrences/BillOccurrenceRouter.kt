package occurrences

import BaseRouter
import com.weesnerdevelopment.shared.auth.InvalidUserReason
import com.weesnerdevelopment.shared.auth.User
import com.weesnerdevelopment.shared.base.GenericResponse
import com.weesnerdevelopment.shared.base.Response.Companion.BadRequest
import com.weesnerdevelopment.shared.base.Response.Companion.NotFound
import com.weesnerdevelopment.shared.base.Response.Companion.Ok
import com.weesnerdevelopment.shared.billMan.BillOccurrence
import com.weesnerdevelopment.shared.billMan.responses.BillOccurrencesResponse
import com.weesnerdevelopment.shared.toJson
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.routing.*
import parse
import respond
import respondError
import respondErrorAuthorizing
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

                return@put respond(response)
            }

            val user = tokenAsUser(service.usersService)?.redacted()?.parse<User>()
                ?: return@put respondErrorAuthorizing(InvalidUserReason.NoUserFound)

            val occurrenceId = call.request.queryParameters["id"]
                ?: return@put respondError(BadRequest("Invalid occurrence id."))

            val payment = call.request.queryParameters["pay"]?.toDoubleOrNull()
                ?: return@put respondError(BadRequest("Invalid amount."))

            val occurrence = service.get {
                service.table.id eq occurrenceId.toInt()
            } ?: return@put respondError(NotFound("No occurrence found for id $occurrenceId."))

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

            respond(response)
        }
    }
}
