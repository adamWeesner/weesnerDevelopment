package occurrences

import auth.UsersService
import diff
import forOwner
import generics.GenericRouter
import history.HistoryService
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.util.pipeline.PipelineContext
import loggedUserData
import occurrencesSharedUsers.OccurrenceSharedUsers
import occurrencesSharedUsers.OccurrenceSharedUsersService
import org.jetbrains.exposed.sql.and
import payments.PaymentsService
import respond
import respondError
import respondErrorAuthorizing
import shared.auth.InvalidUserReason
import shared.base.EarlyResponseException
import shared.base.Response.Companion.BadRequest
import shared.base.Response.Companion.Conflict
import shared.base.Response.Companion.NotFound
import shared.base.Response.Companion.Ok
import shared.billMan.Occurrence
import shared.billMan.Payment
import shared.billMan.responses.OccurrencesResponse

class OccurrenceRouter(
    basePath: String,
    private val occurrencesService: OccurrencesService,
    private val paymentsService: PaymentsService,
    private val usersService: UsersService,
    private val historyService: HistoryService,
    private val sharedUsersService: OccurrenceSharedUsersService
) : GenericRouter<Occurrence, OccurrencesTable>(
    basePath,
    occurrencesService,
    OccurrencesResponse()
) {
    override suspend fun PipelineContext<Unit, ApplicationCall>.putAdditional(
        item: Occurrence,
        updatedItem: Occurrence
    ): Occurrence? {
        var usedItem = updatedItem

        if (item.payments != usedItem.payments) {
            var amountLeft = usedItem.amountLeft.toDouble()
            item.payments.diff(usedItem.payments).apply {
                removed.forEach {
                    amountLeft += it.amount.toDouble()
                    paymentsService.apply {
                        delete(it.id!!) { table.id eq it.id!! }
                    }
                }
                added.forEach {
                    if (amountLeft - it.amount.toDouble() >= 0.0) {
                        amountLeft -= it.amount.toDouble()
                        paymentsService.addForOccurrence(item.id!!, it)
                    } else {
                        throw EarlyResponseException(BadRequest("Payment cannot be more than the amount left of the occurrence."))
                    }
                }
            }

            usedItem = usedItem.copy(amountLeft = amountLeft.toString())
        }

        if (item.sharedUsers != usedItem.sharedUsers) {
            item.sharedUsers.diff(usedItem.sharedUsers).apply {
                added.forEach {
                    sharedUsersService.add(OccurrenceSharedUsers(occurrenceId = item.id!!, userId = it.uuid!!))
                }
                removed.forEach {
                    sharedUsersService.apply {
                        delete(it.id!!) { (table.occurrenceId eq item.id!!) and (table.userId eq it.uuid!!) }
                    }
                }
            }
        }

        val history = handleHistory(item, usedItem, usersService, historyService)
        return usedItem.copy(history = history)
    }

    override fun Route.getDefault() {
        get("/") {
            val uuid = call.loggedUserData()?.getData()?.uuid

            if (call.request.queryParameters.isEmpty())
                return@get call.respondError(BadRequest("Bill id is required. `?bill={billId}`"))

            val billId = call.request.queryParameters["bill"]
            val occurrenceId = call.request.queryParameters["occurrence"]

            if (billId == null && occurrenceId == null)
                return@get call.respondError(BadRequest("Bill id `?bill={billId}` or occurrence id `?occurrence={occurrenceId}` is required."))

            if (occurrenceId != null) {
                val occurrence = service.getSingle { service.table.id eq occurrenceId.toInt() }
                    ?: return@get call.respond(NotFound("Could not get occurrence with $occurrenceId"))

                return@get call.respond(Ok(OccurrencesResponse(listOf(occurrence).forOwner(uuid))))
            }

            if (billId != null) {
                val billOccurrences = occurrencesService.getByBill(billId.toInt())

                call.respond(Ok(billOccurrences))
            }
        }
    }

    override fun Route.deleteDefault() {
        delete("/") {
            if (call.request.queryParameters.isEmpty())
                return@delete call.respondError(BadRequest("Occurrence id is required. `?occurrence={occurrenceId}`"))

            val occurrenceId =
                call.request.queryParameters["occurrence"]
                    ?: return@delete call.respondError(BadRequest("Invalid occurrence id."))

            val id = deleteQualifier(occurrenceId)?.id
                ?: return@delete call.respond(NotFound("Occurrence with an id of $occurrenceId was not found."))

            val removed = service.delete(id) { singleEq(occurrenceId) }

            call.respond(if (removed) Ok("Successfully removed occurrence.") else NotFound("Occurrence with an id of $occurrenceId was not found."))
        }
    }

    fun Route.pay() {
        post("/{occurrenceId}") {
            val user = tokenAsUser(usersService)
            user ?: call.respondErrorAuthorizing(InvalidUserReason.NoUserFound)

            val occurrenceId = call.parameters["occurrenceId"]
                ?: return@post call.respondError(BadRequest("Invalid occurrence id."))

            val payment = call.request.queryParameters["pay"]?.toDoubleOrNull()
                ?: return@post call.respondError(BadRequest("Invalid amount."))

            val occurrence = service.getSingle { service.table.id eq occurrenceId.toInt() }
                ?: return@post call.respondError(NotFound("No occurrence found for id $occurrenceId."))

            if (payment > (occurrence.amountLeft.toDoubleOrNull() ?: 0.0))
                return@post call.respondError(BadRequest("Payment cannot be more than the amount left."))

            val added = paymentsService.addForOccurrence(
                occurrenceId.toInt(),
                Payment(owner = user!!, amount = payment.toString())
            ) ?: return@post call.respondError(Conflict("An error occurred adding the payment."))

            val updatedOccurrence = occurrence.copy(
                amountLeft = occurrence.amountLeft.toDouble().minus(payment).toString(),
                payments = (occurrence.payments ?: listOf()).plus(added)
            )

            service.update(updatedOccurrence) { service.table.id eq occurrenceId.toInt() }

            call.respond(Ok(updatedOccurrence))
        }
    }
}
