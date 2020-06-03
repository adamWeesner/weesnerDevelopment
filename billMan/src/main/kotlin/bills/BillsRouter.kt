package bills

import auth.UsersService
import billSharedUsers.BillSharedUsers
import billSharedUsers.BillSharedUsersService
import colors.ColorsService
import diff
import generics.GenericRouter
import history.HistoryService
import io.ktor.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext
import org.jetbrains.exposed.sql.and
import shared.billMan.Bill

class BillsRouter(
    basePath: String,
    billsService: BillsService,
    private val colorsService: ColorsService,
    private val sharedUsersService: BillSharedUsersService,
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

        if (item.sharedUsers != updatedItem.sharedUsers) {
            item.sharedUsers.diff(updatedItem.sharedUsers).apply {
                added.forEach {
                    sharedUsersService.add(BillSharedUsers(billId = item.id!!, userId = it.uuid!!))
                }
                removed.forEach {
                    sharedUsersService.apply {
                        delete(it.id!!) { (table.billId eq item.id!!) and (table.userId eq it.uuid!!) }
                    }
                }
            }
        }

        val history = handleHistory(item, updatedItem, usersService, historyService)
        return updatedItem.copy(history = history)
    }
}
