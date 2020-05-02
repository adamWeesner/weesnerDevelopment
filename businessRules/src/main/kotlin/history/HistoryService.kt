package history

import auth.UsersService
import dbQuery
import generics.GenericService
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.base.History

class HistoryService(
    private val usersService: UsersService
) : GenericService<History, HistoryTable>(
    HistoryTable
) {
    suspend fun getFor(type: String, typeId: Int) =
        dbQuery { table.select { (table.typeId eq typeId) and (table.type eq type) }.mapNotNull { to(it) } }

    override suspend fun to(row: ResultRow) = History(
        id = row[HistoryTable.id],
        field = row[HistoryTable.field],
        oldValue = row[HistoryTable.oldValue],
        newValue = row[HistoryTable.newValue],
        updatedBy = usersService.getUserByUuid(row[HistoryTable.updatedBy]),
        dateCreated = row[HistoryTable.dateCreated],
        dateUpdated = row[HistoryTable.dateUpdated]
    )

    override fun UpdateBuilder<Int>.assignValues(item: History) {
        this[HistoryTable.field] = item.field
//        this[HistoryTable.type]
//        this[HistoryTable.typeId]
        this[HistoryTable.oldValue] = item.oldValue.toString()
        this[HistoryTable.newValue] = item.newValue.toString()
        this[HistoryTable.updatedBy] = item.updatedBy.uuid!!
    }
}
