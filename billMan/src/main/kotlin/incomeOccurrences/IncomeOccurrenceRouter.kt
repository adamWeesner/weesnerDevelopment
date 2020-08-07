package incomeOccurrences

import auth.UsersService
import forOwner
import generics.GenericRouter
import history.HistoryService
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.util.pipeline.PipelineContext
import loggedUserData
import respond
import respondError
import shared.base.Response.Companion.BadRequest
import shared.base.Response.Companion.NotFound
import shared.base.Response.Companion.Ok
import shared.billMan.Occurrence
import shared.billMan.responses.OccurrencesResponse

class IncomeOccurrenceRouter(
    basePath: String,
    private val occurrencesService: IncomeOccurrencesService,
    private val usersService: UsersService,
    private val historyService: HistoryService
) : GenericRouter<Occurrence, IncomeOccurrencesTable>(
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

    override fun Route.getDefault() {
        get("/") {
            val username = call.loggedUserData()?.getData()?.username

            if (call.request.queryParameters.isEmpty())
                return@get call.respondError(BadRequest("Income id is required. `?income={incomeId}`"))

            val incomeId = call.request.queryParameters["income"]
            val occurrenceId = call.request.queryParameters["occurrence"]

            if (incomeId == null && occurrenceId == null)
                return@get call.respondError(BadRequest("Income id `?income={incomeId}` or occurrence id `?occurrence={occurrenceId}` is required."))

            if (occurrenceId != null) {
                val occurrence = service.getSingle { service.table.id eq occurrenceId.toInt() }
                    ?: return@get call.respond(NotFound("Could not get occurrence with $occurrenceId"))

                return@get call.respond(Ok(OccurrencesResponse(listOf(occurrence).forOwner(username))))
            }

            if (incomeId != null) {
                val billOccurrences = occurrencesService.getByIncome(incomeId.toInt())

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
}
