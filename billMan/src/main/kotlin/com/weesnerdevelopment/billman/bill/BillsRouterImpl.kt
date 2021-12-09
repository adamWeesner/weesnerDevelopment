package com.weesnerdevelopment.billman.bill

import com.weesnerdevelopment.auth.user.getBearerUuid
import com.weesnerdevelopment.billman.category.CategoriesRepository
import com.weesnerdevelopment.businessRules.get
import com.weesnerdevelopment.businessRules.post
import com.weesnerdevelopment.businessRules.put
import com.weesnerdevelopment.businessRules.respond
import com.weesnerdevelopment.shared.base.ServerError
import com.weesnerdevelopment.shared.billMan.Bill
import com.weesnerdevelopment.shared.billMan.responses.BillsResponse
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.routing.*
import java.util.*

@OptIn(KtorExperimentalLocationsAPI::class)
data class BillsRouterImpl(
    val repo: BillsRepository,
    val categoriesRepo: CategoriesRepository
) : BillsRouter {
    /**
     * Reduces typing to get the param for `?id=` :)
     */
    private val ApplicationCall.billId
        get() = request.queryParameters[BillsEndpoint::id.name]

    override fun setup(routing: Routing) {
        routing.apply {
            authenticate {
                get<BillsEndpoint> {
                    val id = call.billId
                    val authUuid = getBearerUuid()!!

                    if (id.isNullOrBlank()) {
                        val bills = repo.getAll(authUuid)
                        return@get respond(HttpStatusCode.OK, BillsResponse(bills))
                    }

                    val idAsUuid = runCatching { UUID.fromString(id) }.getOrNull()

                    if (idAsUuid == null)
                        return@get respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Invalid id '$id' attempting to get bill."
                            )
                        )

                    when (val foundBill = repo.get(authUuid, idAsUuid)) {
                        null -> return@get respond(
                            HttpStatusCode.NotFound,
                            ServerError(
                                HttpStatusCode.NotFound.description,
                                HttpStatusCode.NotFound.value,
                                "No bill with id '$idAsUuid' found."
                            )
                        )
                        else -> return@get respond(HttpStatusCode.OK, foundBill)
                    }
                }

                post<BillsEndpoint, Bill> { bill ->
                    if (bill == null)
                        return@post respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Cannot add invalid bill."
                            )
                        )

                    val newBill = repo.add(bill)
                    if (newBill == null)
                        return@post respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "An error occurred attempting to add bill."
                            )
                        )

                    return@post respond(HttpStatusCode.Created, newBill)
                }

                put<BillsEndpoint, Bill> { bill ->
                    if (bill == null)
                        return@put respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Cannot update invalid bill."
                            )
                        )

                    val updatedBill = repo.update(bill)
                    if (updatedBill == null)
                        return@put respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "An error occurred attempting to update bill."
                            )
                        )

                    return@put respond(HttpStatusCode.Created, updatedBill)
                }

                delete<BillsEndpoint> {
                    val id = call.billId
                    val authUuid = getBearerUuid()!!

                    if (id.isNullOrBlank()) {
                        return@delete respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "Invalid id '$id' attempting to delete bill."
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
                                "Invalid id '$id' attempting to delete bill."
                            )
                        )

                    when (val deletedBill = repo.delete(authUuid, idAsUuid)) {
                        false -> return@delete respond(
                            HttpStatusCode.NotFound,
                            ServerError(
                                HttpStatusCode.NotFound.description,
                                HttpStatusCode.NotFound.value,
                                "No bill with id '$idAsUuid' found."
                            )
                        )
                        else -> return@delete respond(HttpStatusCode.OK, deletedBill)
                    }
                }
            }
        }
    }
}