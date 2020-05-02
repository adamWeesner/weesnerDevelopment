package categories

import HistoryTypes
import auth.UsersService
import generics.GenericService
import history.HistoryService
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.billMan.Category

class CategoriesService(
    private val usersService: UsersService,
    private val historyService: HistoryService
) : GenericService<Category, CategoriesTable>(
    CategoriesTable
) {
    override suspend fun to(row: ResultRow) = Category(
        id = row[CategoriesTable.id],
        owner = row[CategoriesTable.ownerId]?.let { usersService.getUserByUuid(it) },
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
