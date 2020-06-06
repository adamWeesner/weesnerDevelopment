package income

import auth.UsersService
import generics.GenericRouter
import history.HistoryService
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.util.pipeline.PipelineContext
import respond
import respondError
import shared.base.BadRequest
import shared.base.NotFound
import shared.base.Ok
import shared.billMan.Income
import shared.billMan.responses.IncomeResponse

class IncomeRouter(
    basePath: String,
    service: IncomeService,
    private val usersService: UsersService,
    private val historyService: HistoryService
) : GenericRouter<Income, IncomeTable>(
    basePath,
    service,
    IncomeResponse()
) {
    override fun Route.getDefault() {
        get("/") {
            if (call.request.queryParameters.isEmpty()) {
                call.respond(Ok(IncomeResponse(service.getAll())))
            } else {
                val incomeId =
                    call.request.queryParameters["income"]
                        ?: return@get call.respondError(BadRequest("Invalid income id."))

                service.getSingle { service.table.id eq incomeId.toInt() }?.let {
                    call.respond(Ok(IncomeResponse(listOf(it))))
                } ?: call.respond(NotFound("Could not get income with $incomeId"))
            }
        }
    }

    override fun Route.deleteDefault() {
        delete("/") {
            if (call.request.queryParameters.isEmpty())
                return@delete call.respondError(BadRequest("Income id is required. `?income={incomeId}`"))

            val incomeId =
                call.request.queryParameters["income"]
                    ?: return@delete call.respondError(BadRequest("Invalid income id."))

            val id = deleteQualifier(incomeId)?.id
                ?: return@delete call.respond(NotFound("Income with an id of $incomeId was not found."))

            val removed = service.delete(id) { singleEq(incomeId) }

            call.respond(if (removed) Ok("Successfully removed income.") else NotFound("Income with an id of $incomeId was not found."))
        }
    }

    override suspend fun PipelineContext<Unit, ApplicationCall>.putAdditional(
        item: Income,
        updatedItem: Income
    ): Income? {
        val history = handleHistory(item, updatedItem, usersService, historyService)
        return updatedItem.copy(history = history)
    }
}
