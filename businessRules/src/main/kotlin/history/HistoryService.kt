package history

import BaseService
import auth.UsersService
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.auth.User
import shared.base.History

class HistoryService(
    private val usersService: UsersService
) : BaseService<HistoryTable, History>(
    HistoryTable
) {
    private var retrievedUser: User? = null

    suspend fun getFor(type: String?, typeId: Int, user: User? = null) = tryCall {
        table.select {
            table.field regexp "$type $typeId .*"
        }.mapNotNull {
            retrievedUser = user
            toItem(it).also {
                retrievedUser = null
            }
        }
    }

    suspend fun getIdsFor(type: String, typeId: Int) = tryCall {
        table.select {
            table.field regexp "$type $typeId .*"
        }.mapNotNull {
            toItem(it).id
        }
    }

    override suspend fun toItem(row: ResultRow) = History(
        id = row[table.id],
        field = row[table.field],
        oldValue = row[table.oldValue],
        newValue = row[table.newValue],
        updatedBy = (if (row[table.updatedBy] == retrievedUser?.uuid)
            retrievedUser
        else
            usersService.getUserByUuidRedacted(row[table.updatedBy]))
            ?: throw IllegalArgumentException("No user found for history.."),
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
