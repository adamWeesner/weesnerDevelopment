package billMan

import BaseTest
import BuiltRequest
import com.weesnerdevelopment.utils.Path
import io.kotlintest.shouldBe
import io.ktor.http.HttpMethod.Companion.Delete
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpMethod.Companion.Put
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.NoContent
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import parseResponse
import shared.auth.User
import shared.base.History
import shared.billMan.*
import shared.billMan.responses.BillsResponse
import shared.billMan.responses.CategoriesResponse
import shared.billMan.responses.OccurrencesResponse
import shared.taxFetcher.PayPeriod
import java.util.*

class OccurrenceTests : BaseTest({ token ->
    val signedInUser = BuiltRequest(engine, Get, "${Path.User.base}${Path.User.account}", token).asObject<User>()

    BuiltRequest(engine, Post, Path.BillMan.categories, token).send(Category(name = "occurenceCategory"))
    val startCategory =
        BuiltRequest(engine, Get, Path.BillMan.categories, token).asObject<CategoriesResponse>().items?.first()!!

    BuiltRequest(engine, Post, Path.BillMan.bills, token).send(
        Bill(
            owner = signedInUser,
            name = "billStart",
            amount = "1.23",
            categories = listOf(startCategory),
            color = Color(red = 255, green = 255, blue = 255, alpha = 255)
        )
    )
    val startBill = BuiltRequest(engine, Get, Path.BillMan.bills, token).asObject<BillsResponse>().items?.first()!!

    fun newItem(
        amount: Number,
        id: Int? = null,
        billId: String = startBill.id.toString(),
        dueDate: Long = Date().time,
        left: String = amount.toString(),
        every: String = PayPeriod.Weekly.name,
        sharedUsers: List<User>? = null,
        payments: List<Payment>? = null,
        history: List<History>? = null,
        owner: User = signedInUser
    ) = Occurrence(
        id = id,
        owner = owner,
        sharedUsers = sharedUsers,
        itemId = billId,
        dueDate = dueDate,
        amountLeft = left,
        amount = amount.toString(),
        every = every,
        payments = payments,
        history = history
    )

    val path = Path.BillMan.occurrences

    "verify getting base url returns null without bill id" {
        BuiltRequest(engine, Get, path, token).sendStatus<Unit>() shouldBe NoContent
    }

    "verify getting base url returns ok" {
        BuiltRequest(engine, Get, "$path?bill=0", token).sendStatus<Unit>() shouldBe NoContent
    }

    "verify getting base url returns all items in table" {
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(12.34)) shouldBe Created
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(23.45)) shouldBe Created

        with(BuiltRequest(engine, Get, path, token).send<Unit>()) {
            val responseItems = response.content.parseResponse<OccurrencesResponse>()?.items!!
            val item1 = responseItems[responseItems.lastIndex - 1]
            val item2 = responseItems[responseItems.lastIndex]
            response.status() shouldBe OK
            item1.amount shouldBe "12.34"
            item2.amount shouldBe "23.45"
        }
    }

    "verify getting an added item" {
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(34.56)) shouldBe Created

        with(BuiltRequest(engine, Get, "$path?id=3", token).send<Occurrence>()) {
            val addedItems = response.content.parseResponse<OccurrencesResponse>()?.items!!
            response.status() shouldBe OK
            addedItems.size shouldBe 1
            addedItems.first()::class.java shouldBe Occurrence::class.java
            addedItems.first().amount shouldBe "34.56"
        }
    }

    "verify getting an item that does not exist" {
        BuiltRequest(engine, Get, "$path?id=99", token).sendStatus<Unit>() shouldBe NoContent
    }

    "verify adding a new item" {
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(45.67)) shouldBe Created

        with(BuiltRequest(engine, Get, "$path?id=4", token).send<Occurrence>()) {
            val addedItem = response.content.parseResponse<OccurrencesResponse>()?.items?.first()!!
            response.status() shouldBe OK
            addedItem.amount shouldBe "45.67"
        }
    }

    "verify updating an added item" {
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(34.56)) shouldBe Created

        val item = BuiltRequest(engine, Get, "$path?id=5", token).asObject<OccurrencesResponse>().items?.first()!!
        val updatedItem = item.copy(
            amount = "77.77",
            amountLeft = "77.77",
            sharedUsers = listOf(signedInUser)
        )
        BuiltRequest(engine, Put, path, token).sendStatus(updatedItem) shouldBe OK

        with(BuiltRequest(engine, Get, "$path?id=5", token).send<Unit>()) {
            val addedItem = response.content.parseResponse<OccurrencesResponse>()?.items?.first()!!
            val id = addedItem?.id
            val className = addedItem::class.java.simpleName
            val fields = addedItem.history!!.map { it.field }

            response.status() shouldBe OK
            addedItem.amount shouldBe "77.77"
            addedItem.amountLeft shouldBe "77.77"
            fields[0] shouldBe "$className $id amount"
            fields[1] shouldBe "$className $id amountLeft"
            fields[2] shouldBe "$className $id sharedUser"
        }
    }

    "verify updating a non existent item" {
        BuiltRequest(engine, Put, path, token).sendStatus(newItem(5, 99)) shouldBe BadRequest
    }

    "verify updating without an id" {
        BuiltRequest(engine, Put, path, token).sendStatus(newItem(6)) shouldBe BadRequest
    }

    "verify deleting and item that has been added" {
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(7)) shouldBe Created
        BuiltRequest(engine, Delete, "$path?id=6", token).sendStatus<Unit>() shouldBe OK
    }

    "verify deleting item that doesn't exist" {
        BuiltRequest(engine, Delete, "$path?id=99", token).sendStatus<Unit>() shouldBe NotFound
    }

    "verify cannot add payment more than amount left" {
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(45.67)) shouldBe Created

        val item = BuiltRequest(engine, Get, "$path?id=7", token).asObject<OccurrencesResponse>().items?.first()!!
        val updatedItem = item.copy(
            payments = listOf(
                Payment(
                    owner = signedInUser,
                    amount = "60"
                )
            )
        )
        BuiltRequest(engine, Put, path, token).sendStatus(updatedItem) shouldBe BadRequest
    }

    "verify can pay for occurrence" {
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(45.67)) shouldBe Created
        BuiltRequest(engine, Put, "$path?id=8&pay=1.0", token).sendStatus<Unit>() shouldBe OK
        BuiltRequest(engine, Get, "$path?id=8", token).asObject<OccurrencesResponse>().items?.first()?.apply {
            amountLeft.toDouble() shouldBe 44.67
            payments?.size shouldBe 1
        }
    }
})
