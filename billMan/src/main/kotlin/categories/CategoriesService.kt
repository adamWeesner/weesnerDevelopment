package categories

import HistoryTypes
import auth.UsersService
import generics.GenericService
import history.HistoryService
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.billMan.Category

class CategoriesService(
    private val usersService: UsersService,
    private val historyService: HistoryService
) : GenericService<Category, CategoriesTable>(
    CategoriesTable
) {
    override suspend fun delete(id: Int, op: SqlExpressionBuilder.() -> Op<Boolean>): Boolean {
        historyService.run {
            getFor(HistoryTypes.Bill.name, id).mapNotNull { it.id }.forEach { delete(it) { table.id eq it } }
        }
        return super.delete(id, op)
    }

    override suspend fun to(row: ResultRow) = Category(
        id = row[CategoriesTable.id],
        owner = row[CategoriesTable.ownerId]?.let { usersService.getUserByUuidRedacted(it) },
        name = row[CategoriesTable.name],
        history = historyService.getFor(HistoryTypes.Categories.name, row[CategoriesTable.id]),
        dateCreated = row[CategoriesTable.dateCreated],
        dateUpdated = row[CategoriesTable.dateUpdated]
    )

    override fun UpdateBuilder<Int>.assignValues(item: Category) {
        this[CategoriesTable.ownerId] = item.owner?.uuid
        this[CategoriesTable.name] = item.name
    }
}
