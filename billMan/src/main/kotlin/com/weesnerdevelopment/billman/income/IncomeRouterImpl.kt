package com.weesnerdevelopment.billman.income

import auth.AuthValidator
import com.weesnerdevelopment.businessRules.*
import com.weesnerdevelopment.businessRules.get
import com.weesnerdevelopment.shared.billMan.Income
import com.weesnerdevelopment.shared.billMan.responses.IncomeResponse
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.locations.delete
import io.ktor.routing.*
import java.util.*

@OptIn(KtorExperimentalLocationsAPI::class)
data class IncomeRouterImpl(
    val repo: IncomeRepository,
    val authValidator: AuthValidator
) : IncomeRouter {
    /**
     * Reduces typing to get the param for `?id=` :)
     */
    private val ApplicationCall.incomeId
        get() = request.queryParameters[IncomesEndpoint::id.name]

    override fun setup(routing: Routing) {
        routing.apply {
            get<IncomesEndpoint> {
                val id = call.incomeId
                val authUuid = authValidator.getUuid(this)

                if (id.isNullOrBlank()) {
                    val incomes = repo.getAll(authUuid)
                    return@get respond(HttpStatusCode.OK, IncomeResponse(incomes))
                }

                if (runCatching { UUID.fromString(id) }.getOrNull() == null)
                    return@get respondWithError(HttpStatusCode.BadRequest, "Invalid id '$id' attempting to get income.")

                return@get when (val foundIncome = repo.get(authUuid, id)) {
                    null -> respondWithError(HttpStatusCode.NotFound, "No income with id '$id' found.")
                    else -> respond(HttpStatusCode.OK, foundIncome)
                }
            }

            post<IncomesEndpoint, Income> { income ->
                authValidator.getUuid(this)

                if (income == null)
                    return@post respondWithError(HttpStatusCode.BadRequest, "Cannot add invalid income.")

                return@post when (val newIncome = repo.add(income)) {
                    null -> respondWithError(HttpStatusCode.BadRequest, "An error occurred attempting to add income.")
                    else -> respond(HttpStatusCode.Created, newIncome)
                }
            }

            put<IncomesEndpoint, Income> { income ->
                val authUuid = authValidator.getUuid(this)

                if (income == null)
                    return@put respondWithError(HttpStatusCode.BadRequest, "Cannot update invalid income.")

                if (income.owner != authUuid)
                    return@put respondWithError(HttpStatusCode.NotFound, "No income with id '${income.id}' found.")

                return@put when (val updatedIncome = repo.update(income)) {
                    null -> respondWithError(
                        HttpStatusCode.BadRequest,
                        "An error occurred attempting to update income."
                    )
                    else -> respond(HttpStatusCode.OK, updatedIncome)
                }
            }

            delete<IncomesEndpoint> {
                val id = call.incomeId
                val authUuid = authValidator.getUuid(this)

                if (id.isNullOrBlank() || runCatching { UUID.fromString(id) }.getOrNull() == null)
                    return@delete respondWithError(
                        HttpStatusCode.BadRequest,
                        "Invalid id '$id' attempting to delete income."
                    )

                return@delete when (val deletedIncome = repo.delete(authUuid, id)) {
                    false -> respondWithError(HttpStatusCode.NotFound, "No income with id '$id' found.")
                    else -> respond(HttpStatusCode.OK, deletedIncome)
                }
            }
        }
    }
}