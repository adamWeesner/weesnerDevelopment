package com.weesnerdevelopment.billman.bill

import auth.AuthValidator
import com.weesnerdevelopment.billman.category.CategoriesRepository
import com.weesnerdevelopment.businessRules.*
import com.weesnerdevelopment.businessRules.get
import com.weesnerdevelopment.shared.billMan.Bill
import com.weesnerdevelopment.shared.billMan.responses.BillsResponse
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.locations.delete
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
                    return@get respondWithError(HttpStatusCode.BadRequest, "Invalid id '$id' attempting to get bill.")

                return@get when (val foundBill = repo.get(authUuid, id)) {
                    null -> respondWithError(HttpStatusCode.NotFound, "No bill with id '$id' found.")
                    else -> respond(HttpStatusCode.OK, foundBill)
                }
            }

            post<BillsEndpoint, Bill> { bill ->
                authValidator.getUuid(this)

                if (bill == null)
                    return@post respondWithError(HttpStatusCode.BadRequest, "Cannot add invalid bill.")

                return@post when (val newBill = repo.add(bill)) {
                    null -> respondWithError(HttpStatusCode.BadRequest, "An error occurred attempting to add bill.")
                    else -> respond(HttpStatusCode.Created, newBill)
                }
            }

            put<BillsEndpoint, Bill> { bill ->
                val authUuid = authValidator.getUuid(this)

                if (bill == null)
                    return@put respondWithError(HttpStatusCode.BadRequest, "Cannot update invalid bill.")

                if (!bill.sharedUsers.contains(authUuid))
                    return@put respondWithError(HttpStatusCode.NotFound, "No bill with id '${bill.id}' found.")

                return@put when (val updatedBill = repo.update(bill)) {
                    null -> respondWithError(HttpStatusCode.BadRequest, "An error occurred attempting to update bill.")
                    else -> respond(HttpStatusCode.OK, updatedBill)
                }
            }

            delete<BillsEndpoint> {
                val id = call.billId
                val authUuid = authValidator.getUuid(this)

                if (id.isNullOrBlank() || runCatching { UUID.fromString(id) }.getOrNull() == null)
                    return@delete respondWithError(
                        HttpStatusCode.BadRequest,
                        "Invalid id '$id' attempting to delete bill."
                    )

                return@delete when (val deletedBill = repo.delete(authUuid, id)) {
                    false -> respondWithError(HttpStatusCode.NotFound, "No bill with id '$id' found.")
                    else -> respond(HttpStatusCode.OK, deletedBill)
                }
            }
        }
    }
}