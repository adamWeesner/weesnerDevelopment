package com.weesnerdevelopment.billman.income

import com.weesnerdevelopment.auth.user.getBearerUuid
import com.weesnerdevelopment.businessRules.get
import com.weesnerdevelopment.businessRules.post
import com.weesnerdevelopment.businessRules.put
import com.weesnerdevelopment.businessRules.respond
import com.weesnerdevelopment.shared.base.ServerError
import com.weesnerdevelopment.shared.billMan.Income
import com.weesnerdevelopment.shared.billMan.responses.IncomeResponse
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.locations.*
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
                    val authUuid = getBearerUuid()!!

                    if (id.isNullOrBlank()) {
                        val incomes = repo.getAll(authUuid)
                        return@get respond(HttpStatusCode.OK, IncomeResponse(incomes))
                    }

                    val idAsUuid = runCatching { UUID.fromString(id) }.getOrNull()

                    if (idAsUuid == null)
                        return@get respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Invalid id '$id' attempting to get income."
                            )
                        )

                    when (val foundIncome = repo.get(authUuid, idAsUuid)) {
                        null -> return@get respond(
                            HttpStatusCode.NotFound,
                            ServerError(
                                HttpStatusCode.NotFound.description,
                                HttpStatusCode.NotFound.value,
                                "No income with id '$idAsUuid' found."
                            )
                        )
                        else -> return@get respond(HttpStatusCode.OK, foundIncome)
                    }
                }

                post<IncomesEndpoint, Income> { Income ->
                    if (Income == null)
                        return@post respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Cannot add invalid income."
                            )
                        )

                    val newIncome = repo.add(Income)
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

                put<IncomesEndpoint, Income> { Income ->
                    if (Income == null)
                        return@put respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Cannot update invalid income."
                            )
                        )

                    val updatedIncome = repo.update(Income)
                    if (updatedIncome == null)
                        return@put respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "An error occurred attempting to update income."
                            )
                        )

                    return@put respond(HttpStatusCode.Created, updatedIncome)
                }

                delete<IncomesEndpoint> {
                    val id = call.incomeId
                    val authUuid = getBearerUuid()!!

                    if (id.isNullOrBlank()) {
                        return@delete respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Invalid id '$id' attempting to delete income."
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
                                "Invalid id '$id' attempting to delete income."
                            )
                        )

                    when (val deletedIncome = repo.delete(authUuid, idAsUuid)) {
                        false -> return@delete respond(
                            HttpStatusCode.NotFound,
                            ServerError(
                                HttpStatusCode.NotFound.description,
                                HttpStatusCode.NotFound.value,
                                "No income with id '$idAsUuid' found."
                            )
                        )
                        else -> return@delete respond(HttpStatusCode.OK, deletedIncome)
                    }
                }
            }
        }
    }
}