package categories

import BaseService
import auth.UsersService
import diff
import history.HistoryService
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.billMan.Category

class CategoriesService(
    private val usersService: UsersService,
    private val historyService: HistoryService
) : BaseService<CategoriesTable, Category>(
    CategoriesTable
) {
    override val CategoriesTable.connections
        get() = this.leftJoin(usersService.table, {
            ownerId
        }, {
            uuid
        })

    override suspend fun update(item: Category, op: SqlExpressionBuilder.() -> Op<Boolean>): Int? {
        val oldItem = get {
            table.id eq item.id!!
        } ?: return null

        if (item.owner == null)
            return null

        oldItem.diff(item).updates(item.owner!!).forEach {
            historyService.add(it)
        }

        return super.update(item, op)
    }

    override suspend fun toItem(row: ResultRow) = Category(
        id = row[table.id],
        owner = row[table.ownerId]?.let {
            usersService.toItemRedacted(row)
        },
        name = row[table.name],
        dateCreated = row[table.dateCreated],
        dateUpdated = row[table.dateUpdated]
    ).let {
        if (it.owner == null)
            return@let it

        val history = historyService.getFor<Category>(it.id, it.owner)

        return@let if (history == null)
            it
        else
            it.copy(history = history)
    }

    override fun UpdateBuilder<Int>.toRow(item: Category) {
        this[table.ownerId] = item.owner?.uuid
        this[table.name] = item.name
    }
}
