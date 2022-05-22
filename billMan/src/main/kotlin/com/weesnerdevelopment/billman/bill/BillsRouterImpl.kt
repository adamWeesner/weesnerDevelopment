package com.weesnerdevelopment.billman.bill

import auth.AuthValidator
import com.weesnerdevelopment.billman.category.CategoriesRepository
import com.weesnerdevelopment.businessRules.get
import com.weesnerdevelopment.businessRules.post
import com.weesnerdevelopment.businessRules.put
import com.weesnerdevelopment.businessRules.respond
import com.weesnerdevelopment.shared.base.ServerError
import com.weesnerdevelopment.shared.billMan.Bill
import com.weesnerdevelopment.shared.billMan.responses.BillsResponse
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.routing.*
import java.util.*

@OptIn(KtorExperimentalLocationsAPI::class)
data class BillsRouterImpl(
    val repo: BillsRepository,
    val categoriesRepo: CategoriesRepository,
    val authValidator: AuthValidator
) : BillsRouter {
    /**
     * Reduces typing to get the param for `?id=` :)
     */
    private val ApplicationCall.billId
        get() = request.queryParameters[BillsEndpoint::id.name]

    override fun setup(routing: Routing) {
        routing.apply {
            get<BillsEndpoint> {
                val id = call.billId
                val authUuid = authValidator.getUuid(this)

                if (id.isNullOrBlank()) {
                    val bills = repo.getAll(authUuid)
                    return@get respond(HttpStatusCode.OK, BillsResponse(bills))
                }

                if (runCatching { UUID.fromString(id) }.getOrNull() == null)
                    return@get respond(
                        HttpStatusCode.BadRequest,
                        ServerError(
                            HttpStatusCode.BadRequest.description,
                            HttpStatusCode.BadRequest.value,
                            "Invalid id '$id' attempting to get bill."
                        )
                    )

                when (val foundBill = repo.get(authUuid, id)) {
                    null ->
                        return@get respond(
                            HttpStatusCode.NotFound,
                            ServerError(
                                HttpStatusCode.NotFound.description,
                                HttpStatusCode.NotFound.value,
                                "No bill with id '$id' found."
                            )
                        )
                    else ->
                        return@get respond(HttpStatusCode.OK, foundBill)
                }
            }

            post<BillsEndpoint, Bill> { bill ->
                authValidator.getUuid(this)

                if (bill == null)
                    return@post respond(
                        HttpStatusCode.BadRequest,
                        ServerError(
                            HttpStatusCode.BadRequest.description,
                            HttpStatusCode.BadRequest.value,
                            "Cannot add invalid bill."
                        )
                    )

                when (val newBill = repo.add(bill)) {
                    null ->
                        return@post respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "An error occurred attempting to add bill."
                            )
                        )
                    else ->
                        return@post respond(HttpStatusCode.Created, newBill)

                }
            }

            put<BillsEndpoint, Bill> { bill ->
                val authUuid = authValidator.getUuid(this)

                if (bill == null) {
                    return@put respond(
                        HttpStatusCode.BadRequest,
                        ServerError(
                            HttpStatusCode.BadRequest.description,
                            HttpStatusCode.BadRequest.value,
                            "Cannot update invalid bill."
                        )
                    )
                }

                if (!bill.sharedUsers.contains(authUuid)) {
                    return@put respond(
                        HttpStatusCode.NotFound,
                        ServerError(
                            HttpStatusCode.NotFound.description,
                            HttpStatusCode.NotFound.value,
                            "No bill with id '${bill.id}' found."
                        )
                    )
                }

                when (val updatedBill = repo.update(bill)) {
                    null ->
                        return@put respond(
                            HttpStatusCode.BadRequest,
                            ServerError(
                                HttpStatusCode.BadRequest.description,
                                HttpStatusCode.BadRequest.value,
                                "An error occurred attempting to update bill."
                            )
                        )
                    else ->
                        return@put respond(HttpStatusCode.OK, updatedBill)
                }
            }

            delete<BillsEndpoint> {
                val id = call.billId
                val authUuid = authValidator.getUuid(this)

                if (id.isNullOrBlank() || runCatching { UUID.fromString(id) }.getOrNull() == null) {
                    return@delete respond(
                        HttpStatusCode.BadRequest,
                        ServerError(
                            HttpStatusCode.BadRequest.description,
                            HttpStatusCode.BadRequest.value,
                            "Invalid id '$id' attempting to delete bill."
                        )
                    )
                }

                when (val deletedBill = repo.delete(authUuid, id)) {
                    false -> return@delete respond(
                        HttpStatusCode.NotFound,
                        ServerError(
                            HttpStatusCode.NotFound.description,
                            HttpStatusCode.NotFound.value,
                            "No bill with id '$id' found."
                        )
                    )
                    else -> return@delete respond(HttpStatusCode.OK, deletedBill)
                }
            }
        }
    }
}