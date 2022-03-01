package com.weesnerdevelopment.billman.category

import com.weesnerdevelopment.businessRules.*
import com.weesnerdevelopment.businessRules.get
import com.weesnerdevelopment.shared.base.ServerError
import com.weesnerdevelopment.shared.billMan.Category
import com.weesnerdevelopment.shared.billMan.responses.CategoriesResponse
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.locations.delete
import io.ktor.routing.*
import java.util.*

@OptIn(KtorExperimentalLocationsAPI::class)
data class CategoriesRouterImpl(
    val repo: CategoriesRepository
) : CategoriesRouter {
    /**
     * Reduces typing to get the param for `?id=` :)
     */
    private val ApplicationCall.categoryId
        get() = request.queryParameters[CategoriesEndpoint::id.name]

    override fun setup(routing: Routing) {
        routing.apply {
            authenticate {
                get<CategoriesEndpoint> {
                    val id = call.categoryId
                    val userUuid = getBearerUuid().toString()

                    if (id.isNullOrBlank()) {
                        val categories = repo.getAll(userUuid)
                        return@get respond(HttpStatusCode.OK, CategoriesResponse(categories))
                    }

                    if (runCatching { UUID.fromString(id) }.getOrNull() == null) {
                        return@get respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Invalid id '$id' attempting to get category."
                            )
                        )
                    }

                    return@get when (val foundCategory = repo.get(userUuid, id)) {
                        null -> respond(
                            HttpStatusCode.NotFound,
                            ServerError(
                                HttpStatusCode.NotFound.description,
                                HttpStatusCode.NotFound.value,
                                "No category with id '$id' found."
                            )
                        )
                        else -> respond(HttpStatusCode.OK, foundCategory)
                    }
                }

                post<CategoriesEndpoint, Category> { category ->
                    if (category == null)
                        return@post respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Cannot add invalid category."
                            )
                        )

                    val newCategory = repo.add(category)
                    if (newCategory == null) {
                        return@post respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "An error occurred attempting to add category."
                            )
                        )
                    }

                    return@post respond(HttpStatusCode.Created, newCategory)
                }

                put<CategoriesEndpoint, Category> { category ->
                    val userUuid = getBearerUuid().toString()

                    if (category == null) {
                        return@put respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Cannot update invalid category."
                            )
                        )
                    }

                    if (category.owner == null || category.owner != userUuid) {
                        return@put respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Cannot update category."
                            )
                        )
                    }

                    val updatedCategory = repo.update(category)
                    if (updatedCategory == null) {
                        return@put respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "An error occurred attempting to update category."
                            )
                        )
                    }

                    return@put respond(HttpStatusCode.OK, updatedCategory)
                }

                delete<CategoriesEndpoint> {
                    val id = call.categoryId
                    val authUuid = getBearerUuid().toString()

                    if (id.isNullOrBlank() || runCatching { UUID.fromString(id) }.getOrNull() == null) {
                        return@delete respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Invalid id '$id' attempting to delete category."
                            )
                        )
                    }

                    when (val deletedCategory = repo.delete(authUuid, id)) {
                        false -> return@delete respond(
                            HttpStatusCode.NotFound,
                            ServerError(
                                HttpStatusCode.NotFound.description,
                                HttpStatusCode.NotFound.value,
                                "No category with id '$id' found."
                            )
                        )
                        else -> return@delete respond(HttpStatusCode.OK, deletedCategory)
                    }
                }
            }
        }
    }
}