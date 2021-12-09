package com.weesnerdevelopment.billman.income.occurrence

import com.weesnerdevelopment.auth.user.getBearerUuid
import com.weesnerdevelopment.businessRules.get
import com.weesnerdevelopment.businessRules.post
import com.weesnerdevelopment.businessRules.put
import com.weesnerdevelopment.businessRules.respond
import com.weesnerdevelopment.shared.base.ServerError
import com.weesnerdevelopment.shared.billMan.IncomeOccurrence
import com.weesnerdevelopment.shared.billMan.responses.IncomeOccurrencesResponse
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.routing.*
import java.util.*

@OptIn(KtorExperimentalLocationsAPI::class)
data class IncomeOccurrenceRouterImpl(
    val repo: IncomeOccurrenceRepository
) : IncomeOccurrenceRouter {
    /**
     * Reduces typing to get the param for `?id=` :)
     */
    private val ApplicationCall.occurrenceId
        get() = request.queryParameters[IncomeOccurrenceEndpoint::id.name]

    override fun setup(routing: Routing) {
        routing.apply {
            authenticate {
                get<IncomeOccurrenceEndpoint> {
                    val id = call.occurrenceId
                    val userUuid = getBearerUuid()!!

                    if (id.isNullOrBlank()) {
                        val occurrences = repo.getAll(userUuid)
                        return@get respond(HttpStatusCode.OK, IncomeOccurrencesResponse(occurrences))
                    }

                    val idAsUuid = runCatching { UUID.fromString(id) }.getOrNull()

                    if (idAsUuid == null)
                        return@get respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Invalid id '$id' attempting to get income occurrence."
                            )
                        )

                    return@get when (val foundIncomeOccurrence = repo.get(userUuid, idAsUuid)) {
                        null -> respond(
                            HttpStatusCode.NotFound,
                            ServerError(
                                HttpStatusCode.NotFound.description,
                                HttpStatusCode.NotFound.value,
                                "No income occurrence with id '$idAsUuid' found."
                            )
                        )
                        else -> respond(HttpStatusCode.OK, foundIncomeOccurrence)
                    }
                }

                post<IncomeOccurrenceEndpoint, IncomeOccurrence> { incomeOccurrence ->
                    if (incomeOccurrence == null)
                        return@post respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Cannot add invalid income occurrence."
                            )
                        )

                    val newIncomeOccurrence = repo.add(incomeOccurrence)
                    if (newIncomeOccurrence == null)
                        return@post respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "An error occurred attempting to add income occurrence."
                            )
                        )

                    return@post respond(HttpStatusCode.Created, newIncomeOccurrence)
                }

                put<IncomeOccurrenceEndpoint, IncomeOccurrence> { incomeOccurrence ->
                    if (incomeOccurrence == null)
                        return@put respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Cannot update invalid income occurrence."
                            )
                        )

                    val updatedIncomeOccurrence = repo.update(incomeOccurrence)
                    if (updatedIncomeOccurrence == null)
                        return@put respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "An error occurred attempting to update income occurrence."
                            )
                        )

                    return@put respond(HttpStatusCode.Created, updatedIncomeOccurrence)
                }

                delete<IncomeOccurrenceEndpoint> {
                    val id = call.occurrenceId
                    val authUuid = getBearerUuid()!!

                    if (id.isNullOrBlank()) {
                        return@delete respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Invalid id '$id' attempting to delete income occurrence."
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
                                "Invalid id '$id' attempting to delete income occurrence."
                            )
                        )

                    when (val deletedIncomeOccurrence = repo.delete(authUuid, idAsUuid)) {
                        false -> return@delete respond(
                            HttpStatusCode.NotFound,
                            ServerError(
                                HttpStatusCode.NotFound.description,
                                HttpStatusCode.NotFound.value,
                                "No income occurrence with id '$idAsUuid' found."
                            )
                        )
                        else -> return@delete respond(HttpStatusCode.OK, deletedIncomeOccurrence)
                    }
                }
            }
        }
    }
}