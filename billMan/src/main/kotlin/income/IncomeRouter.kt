package income

import auth.UsersService
import generics.GenericRouter
import history.HistoryService
import io.ktor.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext
import shared.billMan.Income

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
    override suspend fun PipelineContext<Unit, ApplicationCall>.putAdditional(
        item: Income,
        updatedItem: Income
    ): Income? {
        val history = handleHistory(item, updatedItem, usersService, historyService)
        return updatedItem.copy(history = history)
    }
}
