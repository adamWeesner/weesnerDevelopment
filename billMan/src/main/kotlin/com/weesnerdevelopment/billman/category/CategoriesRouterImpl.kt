package com.weesnerdevelopment.billman.category

import auth.AuthValidator
import com.weesnerdevelopment.businessRules.get
import com.weesnerdevelopment.businessRules.post
import com.weesnerdevelopment.businessRules.put
import com.weesnerdevelopment.businessRules.respond
import com.weesnerdevelopment.shared.base.Response
import com.weesnerdevelopment.shared.billMan.Category
import com.weesnerdevelopment.shared.billMan.responses.CategoriesResponse
import io.ktor.server.application.*
import io.ktor.server.locations.*
import io.ktor.server.routing.*
import java.util.*

@OptIn(KtorExperimentalLocationsAPI::class)
data class CategoriesRouterImpl(
    val repo: CategoriesRepository,
    val authValidator: AuthValidator
) : CategoriesRouter {
    /**
     * Reduces typing to get the param for `?id=` :)
     */
    private val ApplicationCall.categoryId
        get() = request.queryParameters[CategoriesEndpoint::id.name]

    override fun setup(route: Route) {
        route.apply {
            get<CategoriesEndpoint> {
                val id = call.categoryId
                val userUuid = authValidator.getUuid(this)

                if (id.isNullOrBlank()) {
                    val categories = repo.getAll(userUuid)
                    return@get respond(Response.Ok(CategoriesResponse(categories)))
                }

                if (runCatching { UUID.fromString(id) }.getOrNull() == null)
                    return@get respond(Response.BadRequest("Invalid id '$id' attempting to get category."))

                return@get when (val foundCategory = repo.get(userUuid, id)) {
                    null -> respond(Response.NotFound("No category with id '$id' found."))
                    else -> respond(Response.Ok(foundCategory))
                }
            }

            post<CategoriesEndpoint, Category> { category ->
                if (category == null)
                    return@post respond(Response.BadRequest("Cannot add invalid category."))

                return@post when (val newCategory = repo.add(category)) {
                    null -> respond(Response.BadRequest("An error occurred attempting to add category."))
                    else -> respond(Response.Created(newCategory))
                }
            }

            put<CategoriesEndpoint, Category> { category ->
                val userUuid = authValidator.getUuid(this)

                if (category == null)
                    return@put respond(Response.BadRequest("Cannot update invalid category."))

                if (category.owner == null || category.owner != userUuid)
                    return@put respond(Response.BadRequest("Cannot update category."))

                return@put when (val updatedCategory = repo.update(category)) {
                    null -> respond(Response.BadRequest("An error occurred attempting to update category."))
                    else -> respond(Response.Ok(updatedCategory))
                }
            }

            delete<CategoriesEndpoint> {
                val id = call.categoryId
                val authUuid = authValidator.getUuid(this)

                if (id.isNullOrBlank() || runCatching { UUID.fromString(id) }.getOrNull() == null)
                    return@delete respond(Response.BadRequest("Invalid id '$id' attempting to delete category."))

                return@delete when (val deletedCategory = repo.delete(authUuid, id)) {
                    false -> respond(Response.NotFound("No category with id '$id' found."))
                    else -> respond(Response.Ok(deletedCategory))
                }
            }
        }
    }
}