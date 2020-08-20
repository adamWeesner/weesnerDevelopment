package billMan

import BaseTest
import Path
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
        post(Path.BillMan.income).send(
            Income(
                owner = signedInUser,
                name = "billStart",
                amount = "1.23",
                color = Color(red = 255, green = 255, blue = 255, alpha = 255)
            )
        )
        startIncome = get(Path.BillMan.income).asObject<IncomeResponse>().items?.first()!!
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
        get(path).sendStatus<Unit>() shouldBe NoContent
    }

    @Test
    @Order(2)
    fun `verify getting base url returns all items in table`() {
        post(path).sendStatus(newItem(12.34)) shouldBe Created
        post(path).sendStatus(newItem(23.45)) shouldBe Created

        val request = get(path).send<Unit>()
        val responseItems = request.response.content.parseResponse<IncomeOccurrencesResponse>()?.items!!

        val item1 = responseItems[responseItems.lastIndex - 1]
        val item2 = responseItems[responseItems.lastIndex]
        request.response.status() shouldBe OK
        item1.amount shouldBe "12.34"
        item2.amount shouldBe "23.45"
    }

    @Test
    @Order(3)
    fun `verify getting an added item`() {
        post(path).sendStatus(newItem(34.56)) shouldBe Created

        val request = get(path, 3).send<Unit>()
        val addedItems = request.response.content.parseResponse<IncomeOccurrencesResponse>()?.items

        request.response.status() shouldBe OK
        addedItems?.size shouldBe 1
        addedItems?.first()!!::class.java shouldBe IncomeOccurrence::class.java
        addedItems.first().amount shouldBe "34.56"
    }

    @Test
    @Order(4)
    fun `verify getting an item that does not exist`() {
        get(path, 99).sendStatus<Unit>() shouldBe NoContent
    }

    @Test
    @Order(5)
    fun `verify adding a new item`() {
        post(path).sendStatus(newItem(45.67)) shouldBe Created

        val request = get(path, 4).send<Unit>()
        val addedItem = request.response.content.parseResponse<IncomeOccurrencesResponse>()?.items?.first()!!

        request.response.status() shouldBe OK
        addedItem.amount shouldBe "45.67"
    }

    @Test
    @Order(6)
    fun `verify updating an added item`() {
        post(path).sendStatus(newItem(34.56)) shouldBe Created

        val item = get(path, 5).asObject<IncomeOccurrencesResponse>().items?.first()!!
        val updatedItem = item.copy(amount = "77.77")

        put(path).sendStatus(updatedItem) shouldBe OK

        val request = get(path, 5).send<Unit>()
        val addedItem = request.response.content.parseResponse<IncomeOccurrencesResponse>()?.items?.first()!!

        val id = addedItem.id
        val className = addedItem::class.java.simpleName
        val fields = addedItem.history!!.map { it.field }

        request.response.status() shouldBe OK
        addedItem.amount shouldBe "77.77"
        fields[0] shouldBe "$className $id amount"
    }

    @Test
    @Order(7)
    fun `verify updating a non existent item`() {
        put(path).sendStatus(newItem(5, 99)) shouldBe BadRequest
    }

    @Test
    @Order(8)
    fun `verify deleting and item that has been added`() {
        post(path).sendStatus(newItem(7)) shouldBe Created
        delete(path, 6).sendStatus<Unit>() shouldBe OK
    }

    @Test
    @Order(9)
    fun `verify deleting item that doesn't exist`() {
        delete(path, 99).sendStatus<Unit>() shouldBe NotFound
    }
}
