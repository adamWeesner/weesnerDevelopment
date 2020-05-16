package bills

import HistoryTypes
import auth.UsersService
import billCategories.BillCategoriesService
import billSharedUsers.BillSharedUsersService
import colors.ColorsService
import generics.GenericService
import history.HistoryService
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.billMan.Bill

class BillsService(
    private val sharedUsersService: BillSharedUsersService,
    private val usersService: UsersService,
    private val billCategoriesService: BillCategoriesService,
    private val colorsService: ColorsService,
    private val historyService: HistoryService
) : GenericService<Bill, BillsTable>(
    BillsTable
) {
    override suspend fun to(row: ResultRow) = Bill(
        id = row[BillsTable.id],
        owner = usersService.getUserByUuid(row[BillsTable.ownerId])
            ?: throw IllegalArgumentException("No user found for bill."),
        name = row[BillsTable.name],
        amount = row[BillsTable.amount],
        varyingAmount = row[BillsTable.varyingAmount],
        payoffAmount = row[BillsTable.payoffAmount],
        sharedUsers = sharedUsersService.getByBill(row[BillsTable.id]),
        categories = billCategoriesService.getByBill(row[BillsTable.id]),
        color = colorsService.getByBill(row[BillsTable.id]),
        history = historyService.getFor(HistoryTypes.Bill.name, row[BillsTable.id]),
        dateCreated = row[BillsTable.dateCreated],
        dateUpdated = row[BillsTable.dateUpdated]
    )

    override fun UpdateBuilder<Int>.assignValues(item: Bill) {
        this[BillsTable.ownerId] = item.owner.uuid!!
        this[BillsTable.name] = item.name
        this[BillsTable.amount] = item.amount
        this[BillsTable.varyingAmount] = item.varyingAmount
        this[BillsTable.payoffAmount] = item.payoffAmount
    }
}
