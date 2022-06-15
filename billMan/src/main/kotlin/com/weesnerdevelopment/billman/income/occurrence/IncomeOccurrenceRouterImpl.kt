package com.weesnerdevelopment.billman.income.occurrence

import auth.AuthValidator
import com.weesnerdevelopment.businessRules.*
import com.weesnerdevelopment.businessRules.get
import com.weesnerdevelopment.shared.base.Response
import com.weesnerdevelopment.shared.billMan.IncomeOccurrence
import com.weesnerdevelopment.shared.billMan.responses.IncomeOccurrencesResponse
import io.ktor.server.application.*
import io.ktor.server.locations.*
import io.ktor.server.locations.delete
import io.ktor.server.routing.*
import java.util.*

@OptIn(KtorExperimentalLocationsAPI::class)
data class IncomeOccurrenceRouterImpl(
    val repo: IncomeOccurrenceRepository,
    val authValidator: AuthValidator
) : IncomeOccurrenceRouter {
    /**
     * Reduces typing to get the param for `?id=` :)
     */
    private val ApplicationCall.occurrenceId
        get() = request.queryParameters[IncomeOccurrenceEndpoint::id.name]

    override fun setup(route: Route) {
        route.apply {
            get<IncomeOccurrenceEndpoint> {
                val id = call.occurrenceId
                val userUuid = authValidator.getUuid(this)

                if (id.isNullOrBlank()) {
                    val occurrences = repo.getAll(userUuid)
                    return@get respond(Response.Ok(IncomeOccurrencesResponse(occurrences)))
                }

                if (runCatching { UUID.fromString(id) }.getOrNull() == null)
                    return@get respond(Response.BadRequest("Invalid id '$id' attempting to get income occurrence."))

                return@get when (val foundIncomeOccurrence = repo.get(userUuid, id)) {
                    null -> respond(Response.NotFound("No income occurrence with id '$id' found."))
                    else -> respond(Response.Ok(foundIncomeOccurrence))
                }
            }

            post<IncomeOccurrenceEndpoint, IncomeOccurrence> { incomeOccurrence ->
                val userUuid = authValidator.getUuid(this)

                if (incomeOccurrence == null)
                    return@post respond(Response.BadRequest("Cannot add invalid income occurrence."))

                if (incomeOccurrence.owner != userUuid) {
                    Log.warn("The owner of the income occurrence attempting to add and the bearer token did not match. Bearer id $userUuid income occurrence $incomeOccurrence")
                    return@post respond(Response.BadRequest("Cannot add income occurrence."))
                }

                return@post when (val newIncomeOccurrence = repo.add(incomeOccurrence)) {
                    null -> respond(Response.BadRequest("An error occurred attempting to add income occurrence."))
                    else -> respond(Response.Created(newIncomeOccurrence))
                }
            }

            put<IncomeOccurrenceEndpoint, IncomeOccurrence> { incomeOccurrence ->
                val userUuid = authValidator.getUuid(this)

                if (incomeOccurrence == null)
                    return@put respond(Response.BadRequest("Cannot update invalid income occurrence."))

                if (incomeOccurrence.owner != userUuid) {
                    Log.warn("The owner of the income occurrence attempting to update and the bearer token did not match. Bearer id $userUuid income occurrence $incomeOccurrence")
                    return@put respond(Response.BadRequest("Cannot update income occurrence."))
                }

                return@put when (val updatedIncomeOccurrence = repo.update(incomeOccurrence)) {
                    null -> respond(Response.BadRequest("An error occurred attempting to update income occurrence."))
                    else -> respond(Response.Ok(updatedIncomeOccurrence))
                }
            }

            delete<IncomeOccurrenceEndpoint> {
                val id = call.occurrenceId
                val authUuid = authValidator.getUuid(this)

                if (id.isNullOrBlank() || runCatching { UUID.fromString(id) }.getOrNull() == null)
                    return@delete respond(Response.BadRequest("Invalid id '$id' attempting to delete income occurrence."))

                return@delete when (val deletedIncomeOccurrence = repo.delete(authUuid, id)) {
                    false -> respond(Response.NotFound("No income occurrence with id '$id' found."))
                    else -> respond(Response.Ok(deletedIncomeOccurrence))
                }
            }
        }
    }
}