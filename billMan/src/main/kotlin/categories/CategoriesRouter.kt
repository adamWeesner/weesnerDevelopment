package categories

import auth.UsersService
import generics.GenericRouter
import history.HistoryService
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.util.pipeline.PipelineContext
import org.jetbrains.exposed.sql.lowerCase
import respond
import respondError
import shared.base.BadRequest
import shared.base.NotFound
import shared.base.Ok
import shared.billMan.Category
import shared.billMan.responses.CategoriesResponse

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

    override fun Route.getDefault() {
        get("/") {
            if (call.request.queryParameters.isEmpty()) {
                call.respond(Ok(CategoriesResponse(service.getAll())))
            } else {
                val categoryId =
                    call.request.queryParameters["category"]
                        ?: return@get call.respondError(BadRequest("Invalid category id."))

                service.getSingle { service.table.id eq categoryId.toInt() }?.let {
                    call.respond(Ok(CategoriesResponse(listOf(it))))
                } ?: call.respond(NotFound("Could not get category with $categoryId"))
            }
        }
    }

    override fun Route.deleteDefault() {
        delete("/") {
            if (call.request.queryParameters.isEmpty())
                return@delete call.respondError(BadRequest("Category id is required. `?category={categoryId}`"))

            val categoryId =
                call.request.queryParameters["category"]
                    ?: return@delete call.respondError(BadRequest("Invalid category id."))

            val id = deleteQualifier(categoryId)?.id
                ?: return@delete call.respond(NotFound("Category with an id of $categoryId was not found."))

            val removed = service.delete(id) { singleEq(categoryId) }

            call.respond(if (removed) Ok("Successfully removed category.") else NotFound("Category with an id of $categoryId was not found."))
        }
    }

    override suspend fun PipelineContext<Unit, ApplicationCall>.putAdditional(
        item: Category,
        updatedItem: Category
    ): Category? {
        val history = handleHistory(item, updatedItem, usersService, historyService)
        return updatedItem.copy(history = history)
    }
}
