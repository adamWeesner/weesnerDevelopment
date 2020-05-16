package categories

import auth.UsersService
import generics.GenericRouter
import history.HistoryService
import io.ktor.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext
import org.jetbrains.exposed.sql.lowerCase
import shared.billMan.Category

class CategoriesRouter(
    basePath: String,
    categoriesService: CategoriesService,
    private val usersService: UsersService,
    private val historyService: HistoryService
) : GenericRouter<Category, CategoriesTable>(
    basePath,
    categoriesService,
    CategoriesResponse()
) {
    override suspend fun postQualifier(receivedItem: Category): Category? =
        service.getSingle { service.table.name.lowerCase() eq receivedItem.name.toLowerCase() }

    override suspend fun PipelineContext<Unit, ApplicationCall>.putAdditional(
        item: Category,
        updatedItem: Category
    ): Category? {
        val history = handleHistory(item, updatedItem, usersService, historyService)
        return updatedItem.copy(history = history)
    }
}
