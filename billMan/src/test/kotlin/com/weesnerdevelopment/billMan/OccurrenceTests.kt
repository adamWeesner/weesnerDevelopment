package com.weesnerdevelopment.billMan

import Path.BillMan
import com.weesnerdevelopment.shared.auth.User
import com.weesnerdevelopment.shared.base.History
import com.weesnerdevelopment.shared.billMan.*
import com.weesnerdevelopment.shared.billMan.responses.BillOccurrencesResponse
import com.weesnerdevelopment.shared.billMan.responses.BillsResponse
import com.weesnerdevelopment.shared.billMan.responses.CategoriesResponse
import com.weesnerdevelopment.shared.taxFetcher.PayPeriod
import com.weesnerdevelopment.test.utils.BaseTest
import com.weesnerdevelopment.test.utils.parseResponse
import com.weesnerdevelopment.test.utils.shouldBe
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.NoContent
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import java.util.*

class OccurrenceTests : BaseTest("application-test.conf") {
    lateinit var startBill: Bill

    @BeforeAll
    fun start() {
        post(BillMan.categories).send(Category(name = "occurenceCategory"))

        val startCategory = get(BillMan.categories, 1).asObject<CategoriesResponse>().items?.first()
            ?: throw IllegalArgumentException("Somehow category was null")


        post(BillMan.bills).send(
            Bill(
                owner = signedInUser,
                name = "billStart",
                amount = "1.23",
                categories = listOf(startCategory),
                color = Color(red = 255, green = 255, blue = 255, alpha = 255)
            )
        )
        startBill = get(BillMan.bills).asObject<BillsResponse>().items?.first()!!
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
        get(path).sendStatus<Unit>() shouldBe NoContent
    }

    @Test
    @Order(2)
    fun `verify getting base url returns ok`() {
        get("$path?bill=0").sendStatus<Unit>() shouldBe NoContent
    }

    @Test
    @Order(3)
    fun `verify getting base url returns all items in table`() {
        post(path).sendStatus(newItem(12.34)) shouldBe Created
        post(path).sendStatus(newItem(23.45)) shouldBe Created

        val request = get(path).send<Unit>()
        val responseItems = request.response.content.parseResponse<BillOccurrencesResponse>()?.items!!

        val item1 = responseItems[responseItems.lastIndex - 1]
        val item2 = responseItems[responseItems.lastIndex]
        request.response.status() shouldBe OK
        item1.amount shouldBe "12.34"
        item2.amount shouldBe "23.45"
    }

    @Test
    @Order(4)
    fun `verify getting an added item`() {
        post(path).sendStatus(newItem(34.56)) shouldBe Created

        val request = get(path, 3).send<BillOccurrence>()
        val addedItems = request.response.content.parseResponse<BillOccurrencesResponse>()?.items!!

        request.response.status() shouldBe OK
        addedItems.size shouldBe 1
        addedItems.first()::class.java shouldBe BillOccurrence::class.java
        addedItems.first().amount shouldBe "34.56"
    }

    @Test
    @Order(5)
    fun `verify getting an item that does not exist`() {
        get(path, 99).sendStatus<Unit>() shouldBe NoContent
    }

    @Test
    @Order(6)
    fun `verify adding a new item`() {
        post(path).sendStatus(newItem(45.67)) shouldBe Created

        val request = get(path, 4).send<BillOccurrence>()
        val addedItem = request.response.content.parseResponse<BillOccurrencesResponse>()?.items?.first()!!

        request.response.status() shouldBe OK
        addedItem.amount shouldBe "45.67"
    }

    @Test
    @Order(7)
    fun `verify updating an added item`() {
        post(path).sendStatus(newItem(34.56)) shouldBe Created

        val item = get(path, 5).asObject<BillOccurrencesResponse>().items?.first()!!
        val updatedItem = item.copy(
            amount = "77.77",
            amountLeft = "77.77",
            sharedUsers = listOf(signedInUser)
        )
        put(path).sendStatus(updatedItem) shouldBe OK

        val request = get(path, 5).send<Unit>()
        val addedItem = request.response.content.parseResponse<BillOccurrencesResponse>()?.items?.first()!!

        val id = addedItem.id
        val className = addedItem::class.java.simpleName
        val fields = addedItem.history!!.map { it.field }

        request.response.status() shouldBe OK
        addedItem.amount shouldBe "77.77"
        addedItem.amountLeft shouldBe "77.77"
        fields[0] shouldBe "$className $id amount"
        fields[1] shouldBe "$className $id amountLeft"
        fields[2] shouldBe "$className $id sharedUser"
    }

    @Test
    @Order(8)
    fun `verify updating a non existent item`() {
        put(path).sendStatus(newItem(5, 99)) shouldBe BadRequest
    }

    @Test
    @Order(9)
    fun `verify updating without an id`() {
        put(path).sendStatus(newItem(6)) shouldBe BadRequest
    }

    @Test
    @Order(10)
    fun `verify deleting and item that has been added`() {
        post(path).sendStatus(newItem(7)) shouldBe Created
        delete(path, 6).sendStatus<Unit>() shouldBe OK
    }

    @Test
    @Order(11)
    fun `verify deleting item that doesn't exist`() {
        delete(path, 99).sendStatus<Unit>() shouldBe NotFound
    }

    @Test
    @Order(12)
    fun `verify cannot add payment more than amount left`() {
        post(path).sendStatus(newItem(45.67)) shouldBe Created

        val item = get(path, 7).asObject<BillOccurrencesResponse>().items?.first()!!
        val updatedItem = item.copy(
            payments = listOf(
                Payment(
                    owner = signedInUser,
                    amount = "60"
                )
            )
        )
        put(path).sendStatus(updatedItem) shouldBe BadRequest
    }

    @Test
    @Order(13)
    fun `verify can pay for occurrence`() {
        post(path).sendStatus(newItem(45.67)) shouldBe Created
        put(path, 8, "&pay=1.0").sendStatus<Unit>() shouldBe OK
        get(path, 8).asObject<BillOccurrencesResponse>().items?.first()?.apply {
            amountLeft.toDouble() shouldBe 44.67
            payments?.size shouldBe 1
        }
    }
}
