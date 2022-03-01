package com.weesnerdevelopment.billman.income

import com.weesnerdevelopment.businessRules.*
import com.weesnerdevelopment.businessRules.get
import com.weesnerdevelopment.shared.base.ServerError
import com.weesnerdevelopment.shared.billMan.Income
import com.weesnerdevelopment.shared.billMan.responses.IncomeResponse
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.locations.delete
import io.ktor.routing.*
import java.util.*

@OptIn(KtorExperimentalLocationsAPI::class)
data class IncomeRouterImpl(
    val repo: IncomeRepository
) : IncomeRouter {
    /**
     * Reduces typing to get the param for `?id=` :)
     */
    private val ApplicationCall.incomeId
        get() = request.queryParameters[IncomesEndpoint::id.name]

    override fun setup(routing: Routing) {
        routing.apply {
            authenticate {
                get<IncomesEndpoint> {
                    val id = call.incomeId
                    val authUuid = getBearerUuid().toString()

                    if (id.isNullOrBlank()) {
                        val incomes = repo.getAll(authUuid)
                        return@get respond(HttpStatusCode.OK, IncomeResponse(incomes))
                    }

                    val isValidUuid = runCatching { UUID.fromString(id) }.getOrNull()

                    if (isValidUuid == null)
                        return@get respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Invalid id '$id' attempting to get income."
                            )
                        )

                    when (val foundIncome = repo.get(authUuid, id)) {
                        null -> return@get respond(
                            HttpStatusCode.NotFound,
                            ServerError(
                                HttpStatusCode.NotFound.description,
                                HttpStatusCode.NotFound.value,
                                "No income with id '$id' found."
                            )
                        )
                        else -> return@get respond(HttpStatusCode.OK, foundIncome)
                    }
                }

                post<IncomesEndpoint, Income> { income ->
                    if (income == null)
                        return@post respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Cannot add invalid income."
                            )
                        )

                    val newIncome = repo.add(income)
                    if (newIncome == null)
                        return@post respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "An error occurred attempting to add income."
                            )
                        )

                    return@post respond(HttpStatusCode.Created, newIncome)
                }

                put<IncomesEndpoint, Income> { income ->
                    val authUuid = getBearerUuid().toString()

                    if (income == null)
                        return@put respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Cannot update invalid income."
                            )
                        )

                    if (income.owner != authUuid) {
                        return@put respond(
                            HttpStatusCode.NotFound,
                            ServerError(
                                HttpStatusCode.NotFound.description,
                                HttpStatusCode.NotFound.value,
                                "No income with id '${income.id}' found."
                            )
                        )
                    }

                    val updatedIncome = repo.update(income)
                    if (updatedIncome == null)
                        return@put respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "An error occurred attempting to update income."
                            )
                        )

                    return@put respond(HttpStatusCode.OK, updatedIncome)
                }

                delete<IncomesEndpoint> {
                    val id = call.incomeId
                    val authUuid = getBearerUuid().toString()

                    if (id.isNullOrBlank() || runCatching { UUID.fromString(id) }.getOrNull() == null) {
                        return@delete respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Invalid id '$id' attempting to delete income."
                            )
                        )
                    }

                    when (val deletedIncome = repo.delete(authUuid, id)) {
                        false -> return@delete respond(
                            HttpStatusCode.NotFound,
                            ServerError(
                                HttpStatusCode.NotFound.description,
                                HttpStatusCode.NotFound.value,
                                "No income with id '$id' found."
                            )
                        )
                        else -> return@delete respond(HttpStatusCode.OK, deletedIncome)
                    }
                }
            }
        }
    }
}