package billSharedUsers

import HistoryTypes
import auth.UsersService
import dbQuery
import generics.GenericService
import history.HistoryService
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class BillSharedUsersService(
    private val usersService: UsersService,
    private val historyService: HistoryService
) : GenericService<BillSharedUsers, BillsSharedUsersTable>(
    BillsSharedUsersTable
) {
    suspend fun getByBill(id: Int) =
        dbQuery { table.select { (table.billId eq id) }.mapNotNull { to(it) } }.mapNotNull {
            usersService.getUserByUuid(it.userId)
        }

    override suspend fun to(row: ResultRow) = BillSharedUsers(
        id = row[BillsSharedUsersTable.id],
        billId = row[BillsSharedUsersTable.billId],
        userId = row[BillsSharedUsersTable.userId],
        history = historyService.getFor(HistoryTypes.BillSharedUsers.name, row[BillsSharedUsersTable.id]),
        dateCreated = row[BillsSharedUsersTable.dateCreated],
        dateUpdated = row[BillsSharedUsersTable.dateUpdated]
    )

    override fun UpdateBuilder<Int>.assignValues(item: BillSharedUsers) {
        this[BillsSharedUsersTable.billId] = item.billId
        this[BillsSharedUsersTable.userId] = item.userId
    }
}
