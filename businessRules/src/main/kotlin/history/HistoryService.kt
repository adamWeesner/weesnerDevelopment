package history

import BaseService
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.auth.User
import shared.base.GenericItem
import shared.base.History
import shared.base.InvalidAttributeException

class HistoryService : BaseService<HistoryTable, History>(
    HistoryTable
) {
    override val HistoryTable.connections: Join?
        get() = null

    var retrievedUser: User? = null

    suspend inline fun <reified T : GenericItem> getFor(typeId: Int?, user: User?) = tryCall {
        if (typeId == null)
            throw InvalidAttributeException("typeId")

        if (user?.uuid == null)
            throw InvalidAttributeException("user or uuid")

        table.select {
            (table.field regexp "${T::class.simpleName} $typeId .*") and (table.updatedBy eq user.uuid!!)
        }.mapNotNull {
            retrievedUser = user
            toItem(it).also {
                retrievedUser = null
            }
        }
    }

    suspend inline fun <reified T : GenericItem> getIdsFor(typeId: Int?, user: User?) =
        getFor<T>(typeId, user)?.map { it.id!! }

    override suspend fun toItem(row: ResultRow) = History(
        id = row[table.id],
        field = row[table.field],
        oldValue = row[table.oldValue],
        newValue = row[table.newValue],
        updatedBy = retrievedUser ?: throw InvalidAttributeException("User"),
        dateCreated = row[table.dateCreated],
        dateUpdated = row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: History) {
        this[table.field] = item.field
        this[table.oldValue] = item.oldValue.toString()
        this[table.newValue] = item.newValue.toString()
        this[table.updatedBy] = item.updatedBy.uuid!!
    }
}
