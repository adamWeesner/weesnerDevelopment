package bills

import auth.UsersService
import billSharedUsers.BillSharedUsers
import billSharedUsers.BillSharedUsersService
import colors.ColorsService
import diff
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
import org.jetbrains.exposed.sql.and
import respond
import respondError
import shared.base.Response.Companion.BadRequest
import shared.base.Response.Companion.NotFound
import shared.base.Response.Companion.Ok
import shared.billMan.Bill
import shared.billMan.responses.BillsResponse

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
    override fun Route.getDefault() {
        get("/") {
            val uuid = call.loggedUserData()?.getData()?.uuid

            if (call.request.queryParameters.isEmpty()) {
                call.respond(Ok(BillsResponse(service.getAll().forOwner(uuid))))
            } else {
                val billId =
                    call.request.queryParameters["bill"]
                        ?: return@get call.respondError(BadRequest("Invalid bill id."))

                service.getSingle { service.table.id eq billId.toInt() }?.let {
                    call.respond(Ok(BillsResponse(listOf(it).forOwner(uuid))))
                } ?: call.respond(NotFound("Could not get bill with $billId"))
            }
        }
    }

    override fun Route.deleteDefault() {
        delete("/") {
            if (call.request.queryParameters.isEmpty())
                return@delete call.respondError(BadRequest("Bill id is required. `?bill={billId}`"))

            val billId =
                call.request.queryParameters["bill"]
                    ?: return@delete call.respondError(BadRequest("Invalid bill id."))

            val id = deleteQualifier(billId)?.id
                ?: return@delete call.respond(NotFound("Bill with an id of $billId was not found."))

            val removed = service.delete(id) { singleEq(billId) }

            call.respond(if (removed) Ok("Successfully removed bill.") else NotFound("Bill with an id of $billId was not found."))
        }
    }

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
