package billMan

import BaseTest
import BuiltRequest
import Path.BillMan
import io.ktor.http.HttpMethod.Companion.Delete
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpMethod.Companion.Put
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.NoContent
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import parseResponse
import shared.auth.User
import shared.base.History
import shared.billMan.*
import shared.billMan.responses.BillOccurrencesResponse
import shared.billMan.responses.BillsResponse
import shared.billMan.responses.CategoriesResponse
import shared.taxFetcher.PayPeriod
import shouldBe
import java.util.*

class OccurrenceTests : BaseTest() {
    lateinit var startBill: Bill

    @BeforeAll
    fun start() {
        BuiltRequest(engine, Post, BillMan.categories, token).send(Category(name = "occurenceCategory"))

        val startCategory = BuiltRequest(engine, Get, "${BillMan.categories}?id=1", token)
            .asObject<CategoriesResponse>().items?.first()
            ?: throw IllegalArgumentException("Somehow category was null")


        BuiltRequest(engine, Post, BillMan.bills, token).send(
            Bill(
                owner = signedInUser,
                name = "billStart",
                amount = "1.23",
                categories = listOf(startCategory),
                color = Color(red = 255, green = 255, blue = 255, alpha = 255)
            )
        )
        startBill = BuiltRequest(engine, Get, BillMan.bills, token).asObject<BillsResponse>().items?.first()!!
    }

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
    ) = BillOccurrence(
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

    val path = BillMan.occurrences

    @Test
    @Order(1)
    fun `verify getting base url returns null without bill id`() {
        BuiltRequest(engine, Get, path, token).sendStatus<Unit>() shouldBe NoContent
    }

    @Test
    @Order(2)
    fun `verify getting base url returns ok`() {
        BuiltRequest(engine, Get, "$path?bill=0", token).sendStatus<Unit>() shouldBe NoContent
    }

    @Test
    @Order(3)
    fun `verify getting base url returns all items in table`() {
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(12.34)) shouldBe Created
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(23.45)) shouldBe Created

        with(BuiltRequest(engine, Get, path, token).send<Unit>()) {
            val responseItems = response.content.parseResponse<BillOccurrencesResponse>()?.items!!
            val item1 = responseItems[responseItems.lastIndex - 1]
            val item2 = responseItems[responseItems.lastIndex]
            response.status() shouldBe OK
            item1.amount shouldBe "12.34"
            item2.amount shouldBe "23.45"
        }
    }

    @Test
    @Order(4)
    fun `verify getting an added item`() {
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(34.56)) shouldBe Created

        with(BuiltRequest(engine, Get, "$path?id=3", token).send<BillOccurrence>()) {
            val addedItems = response.content.parseResponse<BillOccurrencesResponse>()?.items!!
            response.status() shouldBe OK
            addedItems.size shouldBe 1
            addedItems.first()::class.java shouldBe BillOccurrence::class.java
            addedItems.first().amount shouldBe "34.56"
        }
    }

    @Test
    @Order(5)
    fun `verify getting an item that does not exist`() {
        BuiltRequest(engine, Get, "$path?id=99", token).sendStatus<Unit>() shouldBe NoContent
    }

    @Test
    @Order(6)
    fun `verify adding a new item`() {
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(45.67)) shouldBe Created

        with(BuiltRequest(engine, Get, "$path?id=4", token).send<BillOccurrence>()) {
            val addedItem = response.content.parseResponse<BillOccurrencesResponse>()?.items?.first()!!
            response.status() shouldBe OK
            addedItem.amount shouldBe "45.67"
        }
    }

    @Test
    @Order(7)
    fun `verify updating an added item`() {
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(34.56)) shouldBe Created

        val item = BuiltRequest(engine, Get, "$path?id=5", token).asObject<BillOccurrencesResponse>().items?.first()!!
        val updatedItem = item.copy(
            amount = "77.77",
            amountLeft = "77.77",
            sharedUsers = listOf(signedInUser)
        )
        BuiltRequest(engine, Put, path, token).sendStatus(updatedItem) shouldBe OK

        with(BuiltRequest(engine, Get, "$path?id=5", token).send<Unit>()) {
            val addedItem = response.content.parseResponse<BillOccurrencesResponse>()?.items?.first()!!
            val id = addedItem.id
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

    @Test
    @Order(8)
    fun `verify updating a non existent item`() {
        BuiltRequest(engine, Put, path, token).sendStatus(newItem(5, 99)) shouldBe BadRequest
    }

    @Test
    @Order(9)
    fun `verify updating without an id`() {
        BuiltRequest(engine, Put, path, token).sendStatus(newItem(6)) shouldBe BadRequest
    }

    @Test
    @Order(10)
    fun `verify deleting and item that has been added`() {
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(7)) shouldBe Created
        BuiltRequest(engine, Delete, "$path?id=6", token).sendStatus<Unit>() shouldBe OK
    }

    @Test
    @Order(11)
    fun `verify deleting item that doesn't exist`() {
        BuiltRequest(engine, Delete, "$path?id=99", token).sendStatus<Unit>() shouldBe NotFound
    }

    @Test
    @Order(12)
    fun `verify cannot add payment more than amount left`() {
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(45.67)) shouldBe Created

        val item = BuiltRequest(engine, Get, "$path?id=7", token).asObject<BillOccurrencesResponse>().items?.first()!!
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

    @Test
    @Order(13)
    fun `verify can pay for occurrence`() {
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(45.67)) shouldBe Created
        BuiltRequest(engine, Put, "$path?id=8&pay=1.0", token).sendStatus<Unit>() shouldBe OK
        BuiltRequest(engine, Get, "$path?id=8", token).asObject<BillOccurrencesResponse>().items?.first()?.apply {
            amountLeft.toDouble() shouldBe 44.67
            payments?.size shouldBe 1
        }
    }
}
