package occurrencesSharedUsers

import BaseService
import auth.UsersService
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.base.InvalidAttributeException

class OccurrenceSharedUsersService(
    private val usersService: UsersService
) : BaseService<OccurrenceSharedUsersTable, OccurrenceSharedUsers>(
    OccurrenceSharedUsersTable
) {
    private val OccurrenceSharedUsersTable.connections
        get() = this.innerJoin(usersService.table, {
            userId
        }, {
            uuid
        })

    suspend fun getByOccurrence(id: Int) = tryCall {
        table.connections.select {
            table.occurrenceId eq id
        }.mapNotNull {
            toItem(it)
        }
    }?.mapNotNull {
        usersService.getUserByUuidRedacted(it.userId)
    }

    suspend fun deleteForOccurrence(occurrenceId: Int) = tryCall {
        table.deleteWhere {
            table.occurrenceId eq occurrenceId
        }
    }

    @Deprecated(
        "You should not be able to update a shared user. Deleting and Adding are the only things that make sense.",
        ReplaceWith("Nothing"),
        DeprecationLevel.ERROR
    )
    override suspend fun update(item: OccurrenceSharedUsers, op: SqlExpressionBuilder.() -> Op<Boolean>): Int? = null

    override suspend fun toItem(row: ResultRow) = OccurrenceSharedUsers(
        id = row[table.id],
        occurrenceId = row[table.occurrenceId],
        userId = usersService.toItemRedacted(row).uuid ?: throw InvalidAttributeException("Uuid"),
        dateCreated = row[table.dateCreated],
        dateUpdated = row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: OccurrenceSharedUsers) {
        this[table.occurrenceId] = item.occurrenceId
        this[table.userId] = item.userId
    }
}
