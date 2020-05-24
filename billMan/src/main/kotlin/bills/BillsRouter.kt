package bills

import auth.UsersService
import colors.ColorsService
import generics.GenericRouter
import history.HistoryService
import io.ktor.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext
import shared.billMan.Bill

class BillsRouter(
    basePath: String,
    billsService: BillsService,
    private val colorsService: ColorsService,
    private val usersService: UsersService,
    private val historyService: HistoryService
) : GenericRouter<Bill, BillsTable>(
    basePath,
    billsService,
    BillsResponse()
) {
    override suspend fun PipelineContext<Unit, ApplicationCall>.putAdditional(
        item: Bill,
        updatedItem: Bill
    ): Bill? {
        if (item.color != updatedItem.color)
            colorsService.update(updatedItem.color) { colorsService.table.id eq updatedItem.color.id!! }

        val history = handleHistory(item, updatedItem, usersService, historyService)
        return updatedItem.copy(history = history)
    }
}
