package com.weesnerdevelopment.billman.category

import com.weesnerdevelopment.auth.user.getBearerUuid
import com.weesnerdevelopment.businessRules.get
import com.weesnerdevelopment.businessRules.post
import com.weesnerdevelopment.businessRules.put
import com.weesnerdevelopment.businessRules.respond
import com.weesnerdevelopment.shared.base.ServerError
import com.weesnerdevelopment.shared.billMan.Category
import com.weesnerdevelopment.shared.billMan.responses.CategoriesResponse
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.locations.*
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
                    val userUuid = getBearerUuid()!!

                    if (id.isNullOrBlank()) {
                        val categories = repo.getAll(userUuid)
                        return@get respond(HttpStatusCode.OK, CategoriesResponse(categories))
                    }

                    val idAsUuid = runCatching { UUID.fromString(id) }.getOrNull()

                    if (idAsUuid == null)
                        return@get respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Invalid id '$id' attempting to get category."
                            )
                        )

                    return@get when (val foundCategory = repo.get(userUuid, idAsUuid)) {
                        null -> respond(
                            HttpStatusCode.NotFound,
                            ServerError(
                                HttpStatusCode.NotFound.description,
                                HttpStatusCode.NotFound.value,
                                "No category with id '$idAsUuid' found."
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
                    if (newCategory == null)
                        return@post respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "An error occurred attempting to add category."
                            )
                        )

                    return@post respond(HttpStatusCode.Created, newCategory)
                }

                put<CategoriesEndpoint, Category> { category ->
                    if (category == null)
                        return@put respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Cannot update invalid category."
                            )
                        )

                    val updatedCategory = repo.update(category)
                    if (updatedCategory == null)
                        return@put respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "An error occurred attempting to update category."
                            )
                        )

                    return@put respond(HttpStatusCode.Created, updatedCategory)
                }

                delete<CategoriesEndpoint> {
                    val id = call.categoryId
                    val authUuid = getBearerUuid()!!

                    if (id.isNullOrBlank()) {
                        return@delete respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Invalid id '$id' attempting to delete category."
                            )
                        )
                    }

                    val idAsUuid = runCatching { UUID.fromString(id) }.getOrNull()

                    if (idAsUuid == null)
                        return@delete respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Invalid id '$id' attempting to delete category."
                            )
                        )

                    when (val deletedCategory = repo.delete(authUuid, idAsUuid)) {
                        false -> return@delete respond(
                            HttpStatusCode.NotFound,
                            ServerError(
                                HttpStatusCode.NotFound.description,
                                HttpStatusCode.NotFound.value,
                                "No category with id '$idAsUuid' found."
                            )
                        )
                        else -> return@delete respond(HttpStatusCode.OK, deletedCategory)
                    }
                }
            }
        }
    }
}