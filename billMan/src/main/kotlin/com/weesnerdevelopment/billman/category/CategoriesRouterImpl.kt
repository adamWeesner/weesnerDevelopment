package com.weesnerdevelopment.billman.category

import auth.AuthValidator
import com.weesnerdevelopment.businessRules.get
import com.weesnerdevelopment.businessRules.post
import com.weesnerdevelopment.businessRules.put
import com.weesnerdevelopment.businessRules.respond
import com.weesnerdevelopment.shared.billMan.Category
import com.weesnerdevelopment.shared.billMan.responses.CategoriesResponse
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.routing.*
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

    override fun setup(routing: Routing) {
        routing.apply {
            get<CategoriesEndpoint> {
                val id = call.categoryId
                val userUuid = authValidator.getUuid(this)

                if (id.isNullOrBlank()) {
                    val categories = repo.getAll(userUuid)
                    return@get respond(HttpStatusCode.OK, CategoriesResponse(categories))
                }

                if (runCatching { UUID.fromString(id) }.getOrNull() == null)
                    return@get respond(HttpStatusCode.BadRequest, "Invalid id '$id' attempting to get category.")

                return@get when (val foundCategory = repo.get(userUuid, id)) {
                    null -> respond(HttpStatusCode.NotFound, "No category with id '$id' found.")
                    else -> respond(HttpStatusCode.OK, foundCategory)
                }
            }

            post<CategoriesEndpoint, Category> { category ->
                if (category == null)
                    return@post respond(HttpStatusCode.BadRequest, "Cannot add invalid category.")

                return@post when (val newCategory = repo.add(category)) {
                    null -> respond(HttpStatusCode.BadRequest, "An error occurred attempting to add category.")
                    else -> respond(HttpStatusCode.Created, newCategory)
                }
            }

            put<CategoriesEndpoint, Category> { category ->
                val userUuid = authValidator.getUuid(this)

                if (category == null)
                    return@put respond(HttpStatusCode.BadRequest, "Cannot update invalid category.")

                if (category.owner == null || category.owner != userUuid)
                    return@put respond(HttpStatusCode.BadRequest, "Cannot update category.")

                return@put when (val updatedCategory = repo.update(category)) {
                    null -> respond(HttpStatusCode.BadRequest, "An error occurred attempting to update category.")
                    else -> respond(HttpStatusCode.OK, updatedCategory)
                }
            }

            delete<CategoriesEndpoint> {
                val id = call.categoryId
                val authUuid = authValidator.getUuid(this)

                if (id.isNullOrBlank() || runCatching { UUID.fromString(id) }.getOrNull() == null)
                    return@delete respond(HttpStatusCode.BadRequest, "Invalid id '$id' attempting to delete category.")

                return@delete when (val deletedCategory = repo.delete(authUuid, id)) {
                    false -> respond(HttpStatusCode.NotFound, "No category with id '$id' found.")
                    else -> respond(HttpStatusCode.OK, deletedCategory)
                }
            }
        }
    }
}