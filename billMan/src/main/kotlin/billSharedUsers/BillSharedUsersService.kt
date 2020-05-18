package billSharedUsers

import auth.UsersService
import dbQuery
import generics.GenericService
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class BillSharedUsersService(
    private val usersService: UsersService
) : GenericService<BillSharedUsers, BillsSharedUsersTable>(
    BillsSharedUsersTable
) {
    suspend fun getByBill(id: Int) =
        dbQuery { table.select { (table.billId eq id) }.mapNotNull { to(it) } }.mapNotNull {
            usersService.getUserByUuidRedacted(it.userId)
        }

    suspend fun deleteForBill(billId: Int) = dbQuery {
        table.select { (table.billId eq billId) }.mapNotNull { to(it).id }
    }.forEach { delete(it) { table.id eq it } }

    override suspend fun to(row: ResultRow) = BillSharedUsers(
        id = row[BillsSharedUsersTable.id],
        billId = row[BillsSharedUsersTable.billId],
        userId = row[BillsSharedUsersTable.userId],
        dateCreated = row[BillsSharedUsersTable.dateCreated],
        dateUpdated = row[BillsSharedUsersTable.dateUpdated]
    )

    override fun UpdateBuilder<Int>.assignValues(item: BillSharedUsers) {
        this[BillsSharedUsersTable.billId] = item.billId
        this[BillsSharedUsersTable.userId] = item.userId
    }
}
