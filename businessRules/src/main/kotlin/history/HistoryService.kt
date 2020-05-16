package history

import auth.UsersService
import dbQuery
import generics.GenericService
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.auth.User
import shared.base.History
import shared.fromJson

class HistoryService(
    private val usersService: UsersService
) : GenericService<History, HistoryTable>(
    HistoryTable
) {
    suspend fun getFor(type: String, typeId: Int) =
        dbQuery { table.select { table.field like "$type $typeId .*" }.mapNotNull { to(it) } }

    override suspend fun to(row: ResultRow) = History(
        id = row[HistoryTable.id],
        field = row[HistoryTable.field],
        oldValue = row[HistoryTable.oldValue],
        newValue = row[HistoryTable.newValue],
        updatedBy = usersService.getUserByUuid(row[HistoryTable.updatedBy])?.redacted()?.fromJson<User>()
            ?: throw IllegalArgumentException("No user found for history.."),
        dateCreated = row[HistoryTable.dateCreated],
        dateUpdated = row[HistoryTable.dateUpdated]
    )

    override fun UpdateBuilder<Int>.assignValues(item: History) {
        this[HistoryTable.field] = item.field
        this[HistoryTable.oldValue] = item.oldValue.toString()
        this[HistoryTable.newValue] = item.newValue.toString()
        this[HistoryTable.updatedBy] = item.updatedBy.uuid!!
    }
}
