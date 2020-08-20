package billSharedUsers

import BaseService
import auth.UsersService
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class BillSharedUsersService(
    private val usersService: UsersService
) : BaseService<BillsSharedUsersTable, BillSharedUsers>(
    BillsSharedUsersTable
) {
    override val BillsSharedUsersTable.connections: Join?
        get() = null

    suspend fun getByBill(id: Int) = getAll {
        table.billId eq id
    }?.mapNotNull {
        usersService.getUserByUuidRedacted(it[table.userId])
    }

    suspend fun deleteForBill(billId: Int) = tryCall {
        table.deleteWhere {
            table.billId eq billId
        }
    }

    @Deprecated(
        "You should not be able to update a shared user. Deleting and Adding are the only things that make sense.",
        ReplaceWith("Nothing"),
        DeprecationLevel.ERROR
    )
    override suspend fun update(item: BillSharedUsers, op: SqlExpressionBuilder.() -> Op<Boolean>): Int? {
        return null
    }

    override suspend fun toItem(row: ResultRow) = BillSharedUsers(
        id = row[table.id],
        billId = row[table.billId],
        userId = row[table.userId],
        dateCreated = row[table.dateCreated],
        dateUpdated = row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: BillSharedUsers) {
        this[table.billId] = item.billId
        this[table.userId] = item.userId
    }
}
