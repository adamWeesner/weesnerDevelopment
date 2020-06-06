package bills

import HistoryTypes
import auth.UsersService
import billCategories.BillCategoriesService
import billCategories.BillCategory
import billSharedUsers.BillSharedUsers
import billSharedUsers.BillSharedUsersService
import colors.ColorsService
import dbQuery
import generics.GenericService
import history.HistoryService
import model.ChangeType
import occurrences.OccurrencesService
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.base.InvalidAttributeException
import shared.billMan.Bill

class BillsService(
    private val sharedUsersService: BillSharedUsersService,
    private val usersService: UsersService,
    private val billCategoriesService: BillCategoriesService,
    private val occurrencesService: OccurrencesService,
    private val colorsService: ColorsService,
    private val historyService: HistoryService
) : GenericService<Bill, BillsTable>(
    BillsTable
) {
    override suspend fun add(item: Bill): Bill? {
        val key = dbQuery {
            table.insert {
                it.assignValues(item)
                it[dateCreated] = System.currentTimeMillis()
                it[dateUpdated] = System.currentTimeMillis()
            } get table.id
        }

        item.sharedUsers?.forEach {
            sharedUsersService.add(BillSharedUsers(billId = key, userId = it.uuid!!))
        }
        item.categories.forEach {
            billCategoriesService.add(BillCategory(billId = key, categoryId = it.id!!))
        }
        colorsService.add(key, item.color)

        return getSingle { table.id eq key }?.also {
            onChange(ChangeType.Create, key, it)
        }
    }

    override suspend fun delete(id: Int, op: SqlExpressionBuilder.() -> Op<Boolean>): Boolean {
        sharedUsersService.deleteForBill(id)
        billCategoriesService.deleteForBill(id)
        colorsService.deleteForBill(id)
        occurrencesService.deleteForBill(id)
        historyService.apply { getIdsFor(HistoryTypes.Bill.name, id).forEach { delete(it) { table.id eq it } } }
        return super.delete(id, op)
    }

    override suspend fun to(row: ResultRow) = Bill(
        id = row[BillsTable.id],
        owner = usersService.getUserByUuidRedacted(row[BillsTable.ownerId]) ?: throw InvalidAttributeException("User"),
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
        this[BillsTable.ownerId] = item.owner.uuid ?: throw InvalidAttributeException("Uuid")
        this[BillsTable.name] = item.name
        this[BillsTable.amount] = item.amount
        this[BillsTable.varyingAmount] = item.varyingAmount
        this[BillsTable.payoffAmount] = item.payoffAmount
    }
}
