package bills

import HistoryTypes
import auth.UsersService
import billSharedUsers.BillSharedUsers
import billSharedUsers.BillSharedUsersService
import colors.ColorsService
import diff
import generics.BadRequest
import generics.GenericRouter
import generics.NotFound
import generics.Ok
import history.HistoryService
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.delete
import io.ktor.util.pipeline.PipelineContext
import org.jetbrains.exposed.sql.and
import respond
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
            item.sharedUsers.diff(updatedItem.sharedUsers).also(::println).apply {
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

    override fun Route.deleteDefault() {
        delete("/{item}") {
            val param = call.parameters["item"] ?: return@delete call.respond(BadRequest("Invalid param."))

            val id = deleteQualifier(param)?.id
                ?: return@delete call.respond(NotFound("Item matching $param was not found."))

            colorsService.deleteForBill(id)
            sharedUsersService.deleteForBill(id)
            historyService.apply {
                getFor(HistoryTypes.Bill.name, id).forEach {
                    delete(id) { table.field like "Bill $id .*" }
                }
            }

            val removed = service.delete(id) { singleEq(param) }

            call.respond(if (removed) Ok("Successfully removed item.") else NotFound("Item matching $param was not found."))
        }
    }
}
