package billMan

import BaseTest
import BuiltRequest
import com.weesnerdevelopment.utils.Path
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
import shared.billMan.Color
import shared.billMan.Income
import shared.billMan.IncomeOccurrence
import shared.billMan.responses.IncomeOccurrencesResponse
import shared.billMan.responses.IncomeResponse
import shared.taxFetcher.PayPeriod
import shouldBe
import java.util.*

class IncomeOccurrenceTests : BaseTest() {
    lateinit var startIncome: Income

    @BeforeAll
    fun start() {
        BuiltRequest(engine, Post, Path.BillMan.income, token).send(
            Income(
                owner = signedInUser,
                name = "billStart",
                amount = "1.23",
                color = Color(red = 255, green = 255, blue = 255, alpha = 255)
            )
        )
        startIncome = BuiltRequest(engine, Get, Path.BillMan.income, token).asObject<IncomeResponse>().items?.first()!!
    }

    fun newItem(
        amount: Number,
        id: Int? = null,
        incomeId: String = startIncome.id.toString(),
        dueDate: Long = Date().time,
        every: String = PayPeriod.Weekly.name,
        history: List<History>? = null,
        owner: User = signedInUser
    ) = IncomeOccurrence(
        id = id,
        owner = owner,
        sharedUsers = null,
        itemId = incomeId,
        dueDate = dueDate,
        amountLeft = "0",
        amount = amount.toString(),
        every = every,
        payments = null,
        history = history
    )

    val path = Path.BillMan.incomeOccurrences

    @Test
    @Order(1)
    fun `verify getting base url`() {
        BuiltRequest(engine, Get, path, token).sendStatus<Unit>() shouldBe NoContent
    }

    @Test
    @Order(2)
    fun `verify getting base url returns all items in table`() {
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(12.34)) shouldBe Created
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(23.45)) shouldBe Created

        with(BuiltRequest(engine, Get, path, token).send<Unit>()) {
            val responseItems = response.content.parseResponse<IncomeOccurrencesResponse>()?.items!!
            val item1 = responseItems[responseItems.lastIndex - 1]
            val item2 = responseItems[responseItems.lastIndex]
            response.status() shouldBe OK
            item1.amount shouldBe "12.34"
            item2.amount shouldBe "23.45"
        }
    }

    @Test
    @Order(3)
    fun `verify getting an added item`() {
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(34.56)) shouldBe Created

        with(BuiltRequest(engine, Get, "$path?id=3", token).send<Unit>()) {
            val addedItems = response.content.parseResponse<IncomeOccurrencesResponse>()?.items
            response.status() shouldBe OK
            addedItems?.size shouldBe 1
            addedItems?.first()!!::class.java shouldBe IncomeOccurrence::class.java
            addedItems.first().amount shouldBe "34.56"
        }
    }

    @Test
    @Order(4)
    fun `verify getting an item that does not exist`() {
        BuiltRequest(engine, Get, "$path?id=99", token).sendStatus<Unit>() shouldBe NoContent
    }

    @Test
    @Order(5)
    fun `verify adding a new item`() {
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(45.67)) shouldBe Created

        with(BuiltRequest(engine, Get, "$path?id=4", token).send<Unit>()) {
            val addedItem = response.content.parseResponse<IncomeOccurrencesResponse>()?.items?.first()!!
            response.status() shouldBe OK
            addedItem.amount shouldBe "45.67"
        }
    }

    @Test
    @Order(6)
    fun `verify updating an added item`() {
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(34.56)) shouldBe Created

        val item =
            BuiltRequest(engine, Get, "$path?id=5", token).asObject<IncomeOccurrencesResponse>().items?.first()!!
        val updatedItem = item.copy(amount = "77.77")

        BuiltRequest(engine, Put, path, token).sendStatus(updatedItem) shouldBe OK

        with(BuiltRequest(engine, Get, "$path?id=5", token).send<Unit>()) {
            val addedItem = response.content.parseResponse<IncomeOccurrencesResponse>()?.items?.first()!!
            val id = addedItem.id
            val className = addedItem::class.java.simpleName
            val fields = addedItem.history!!.map { it.field }

            response.status() shouldBe OK
            addedItem.amount shouldBe "77.77"
            fields[0] shouldBe "$className $id amount"
        }
    }

    @Test
    @Order(7)
    fun `verify updating a non existent item`() {
        BuiltRequest(engine, Put, path, token).sendStatus(newItem(5, 99)) shouldBe BadRequest
    }

    @Test
    @Order(8)
    fun `verify deleting and item that has been added`() {
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(7)) shouldBe Created
        BuiltRequest(engine, Delete, "$path?id=6", token).sendStatus<Unit>() shouldBe OK
    }

    @Test
    @Order(9)
    fun `verify deleting item that doesn't exist`() {
        BuiltRequest(engine, Delete, "$path?id=99", token).sendStatus<Unit>() shouldBe NotFound
    }
}
