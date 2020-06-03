package occurrencesSharedUsers

import auth.UsersService
import dbQuery
import generics.GenericService
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class OccurrenceSharedUsersService(
    private val usersService: UsersService
) : GenericService<OccurrenceSharedUsers, OccurrenceSharedUsersTable>(
    OccurrenceSharedUsersTable
) {
    suspend fun getByOccurrence(id: Int) =
        dbQuery { table.select { (OccurrenceSharedUsersTable.occurrenceId eq id) }.mapNotNull { to(it) } }.mapNotNull {
            usersService.getUserByUuidRedacted(it.userId)
        }

    suspend fun deleteForOccurrence(billId: Int) = dbQuery {
        table.select { (table.occurrenceId eq billId) }.mapNotNull { to(it).id }
    }.forEach { delete(it) { table.id eq it } }

    @Deprecated(
        "You should not be able to update a shared user. Deleting and Adding are the only things that make sense.",
        ReplaceWith("Nothing"),
        DeprecationLevel.ERROR
    )
    override suspend fun update(
        item: OccurrenceSharedUsers,
        op: SqlExpressionBuilder.() -> Op<Boolean>
    ): OccurrenceSharedUsers? {
        return null
    }

    override suspend fun to(row: ResultRow) = OccurrenceSharedUsers(
        id = row[OccurrenceSharedUsersTable.id],
        occurrenceId = row[OccurrenceSharedUsersTable.occurrenceId],
        userId = row[OccurrenceSharedUsersTable.userId],
        dateCreated = row[OccurrenceSharedUsersTable.dateCreated],
        dateUpdated = row[OccurrenceSharedUsersTable.dateUpdated]
    )

    override fun UpdateBuilder<Int>.assignValues(item: OccurrenceSharedUsers) {
        this[OccurrenceSharedUsersTable.occurrenceId] = item.occurrenceId
        this[OccurrenceSharedUsersTable.userId] = item.userId
    }
}
