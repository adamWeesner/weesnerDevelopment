package occurrences

import auth.InvalidUserReason
import auth.UsersService
import generics.BadRequest
import generics.Conflict
import generics.GenericRouter
import generics.NotFound
import history.HistoryService
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.util.pipeline.PipelineContext
import payments.PaymentsService
import respondError
import respondErrorAuthorizing
import shared.billMan.Occurrence
import shared.billMan.Payment

class OccurrenceRouter(
    basePath: String,
    occurrencesService: OccurrencesService,
    private val paymentsService: PaymentsService,
    private val usersService: UsersService,
    private val historyService: HistoryService
) : GenericRouter<Occurrence, OccurrencesTable>(
    basePath,
    occurrencesService,
    OccurrencesResponse()
) {
    override suspend fun PipelineContext<Unit, ApplicationCall>.putAdditional(
        item: Occurrence,
        updatedItem: Occurrence
    ): Occurrence? {
        val history = handleHistory(item, updatedItem, usersService, historyService)
        return updatedItem.copy(history = history)
    }

    fun Route.pay() {
        post("{occurrenceId}/pay/{amount}") {
            val user = tokenAsUser(usersService)
            user ?: call.respondErrorAuthorizing(InvalidUserReason.NoUserFound)

            val occurrenceId = call.parameters["occurrenceId"]
                ?: return@post call.respondError(BadRequest("Invalid occurrence id."))

            val payment = call.parameters["amount"]?.toDoubleOrNull()
                ?: return@post call.respondError(BadRequest("Invalid amount."))

            val occurrence = service.getSingle { service.table.id eq occurrenceId.toInt() }
                ?: return@post call.respondError(NotFound("No occurrence found for id $occurrenceId."))

            if (payment > (occurrence.amountLeft.toDoubleOrNull() ?: 0.0))
                return@post call.respondError(BadRequest("Payment cannot be more than the amount left."))

            val added = paymentsService.add(Payment(owner = user!!, amount = payment.toString()))
                ?: return@post call.respondError(Conflict("An error occurred adding the payment."))

            service.update(
                occurrence.copy(
                    amountLeft = (occurrence.amountLeft.toDouble() - payment).toString(),
                    payments = (occurrence.payments ?: listOf()).plus(added)
                )
            ) { service.table.id eq occurrenceId.toInt() }

            call.respond(HttpStatusCode.Created, added)
        }
    }
}
