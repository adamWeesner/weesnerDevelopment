package occurrencesSharedUsers

import auth.UsersService
import dbQuery
import generics.GenericService
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class OccurrenceSharedUsersService(
    private val usersService: UsersService
) : GenericService<OccurrenceSharedUsers, OccurrenceSharedUsersTable>(
    OccurrenceSharedUsersTable
) {
    suspend fun getByOccurrence(id: Int) =
        dbQuery { table.select { (OccurrenceSharedUsersTable.occurrenceId eq id) }.mapNotNull { to(it) } }.mapNotNull {
            usersService.getUserByUuid(it.userId)
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
