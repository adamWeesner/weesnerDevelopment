package com.weesnerdevelopment.billman.income

import auth.AuthValidator
import com.weesnerdevelopment.businessRules.get
import com.weesnerdevelopment.businessRules.post
import com.weesnerdevelopment.businessRules.put
import com.weesnerdevelopment.businessRules.respond
import com.weesnerdevelopment.shared.base.Response
import com.weesnerdevelopment.shared.billMan.Income
import com.weesnerdevelopment.shared.billMan.responses.IncomeResponse
import io.ktor.server.application.*
import io.ktor.server.locations.*
import io.ktor.server.routing.*
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
                    return@get respond(Response.Ok(IncomeResponse(incomes)))
                }

                if (runCatching { UUID.fromString(id) }.getOrNull() == null)
                    return@get respond(Response.BadRequest("Invalid id '$id' attempting to get income."))

                return@get when (val foundIncome = repo.get(authUuid, id)) {
                    null -> respond(Response.NotFound("No income with id '$id' found."))
                    else -> respond(Response.Ok(foundIncome))
                }
            }

            post<IncomesEndpoint, Income> { income ->
                authValidator.getUuid(this)

                if (income == null)
                    return@post respond(Response.BadRequest("Cannot add invalid income."))

                return@post when (val newIncome = repo.add(income)) {
                    null -> respond(Response.BadRequest("An error occurred attempting to add income."))
                    else -> respond(Response.Created(newIncome))
                }
            }

            put<IncomesEndpoint, Income> { income ->
                val authUuid = authValidator.getUuid(this)

                if (income == null)
                    return@put respond(Response.BadRequest("Cannot update invalid income."))

                if (income.owner != authUuid)
                    return@put respond(Response.NotFound("No income with id '${income.id}' found."))

                return@put when (val updatedIncome = repo.update(income)) {
                    null -> respond(Response.BadRequest("An error occurred attempting to update income."))
                    else -> respond(Response.Ok(updatedIncome))
                }
            }

            delete<IncomesEndpoint> {
                val id = call.incomeId
                val authUuid = authValidator.getUuid(this)

                if (id.isNullOrBlank() || runCatching { UUID.fromString(id) }.getOrNull() == null)
                    return@delete respond(Response.BadRequest("Invalid id '$id' attempting to delete income."))

                return@delete when (val deletedIncome = repo.delete(authUuid, id)) {
                    false -> respond(Response.NotFound("No income with id '$id' found."))
                    else -> respond(Response.Ok(deletedIncome))
                }
            }
        }
    }
}