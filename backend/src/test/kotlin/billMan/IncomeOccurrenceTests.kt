package billMan

import BaseTest
import BuiltRequest
import com.weesnerdevelopment.utils.Path
import io.kotlintest.shouldBe
import io.ktor.http.HttpMethod.Companion.Delete
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpMethod.Companion.Put
import io.ktor.http.HttpStatusCode
import parseResponse
import shared.auth.User
import shared.base.History
import shared.billMan.Color
import shared.billMan.Income
import shared.billMan.Occurrence
import shared.billMan.responses.OccurrencesResponse
import shared.taxFetcher.PayPeriod
import java.util.*

class IncomeOccurrenceTests : BaseTest({ token ->
    val signedInUser = BuiltRequest(engine, Get, "${Path.User.base}${Path.User.account}", token).asObject<User>()

    val startIncome = BuiltRequest(engine, Post, Path.BillMan.income, token).asObject(
        Income(
            owner = signedInUser,
            name = "billStart",
            amount = "1.23",
            color = Color(red = 255, green = 255, blue = 255, alpha = 255)
        )
    )

    fun newItem(
        amount: Number,
        id: Int? = null,
        incomeId: String = startIncome.id.toString(),
        dueDate: Long = Date().time,
        every: String = PayPeriod.Weekly.name,
        history: List<History>? = null,
        owner: User = signedInUser
    ) = Occurrence(
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

    "verify getting base url returns null without bill id" {
        BuiltRequest(engine, Get, path, token).sendStatus<Unit>() shouldBe HttpStatusCode.BadRequest
    }

    "verify getting base url returns ok" {
        BuiltRequest(engine, Get, "$path?income=0", token).sendStatus<Unit>() shouldBe HttpStatusCode.OK
    }

    "verify getting base url returns all items in table" {
        val addedItem = BuiltRequest(engine, Post, path, token).asObject(newItem(12.34))
        BuiltRequest(engine, Post, path, token).send(newItem(23.45))
        with(BuiltRequest(engine, Get, "$path?income=${addedItem.itemId}", token).send<Unit>()) {
            val responseItems = response.content.parseResponse<OccurrencesResponse>()?.items
                ?: throw IllegalArgumentException("Occurrence response should not be null..")
            val item1 = responseItems[responseItems.lastIndex - 1]
            val item2 = responseItems[responseItems.lastIndex]
            response.status() shouldBe HttpStatusCode.OK
            item1.amount shouldBe "12.34"
            item2.amount shouldBe "23.45"
        }
    }

    "verify getting an added item" {
        val item = BuiltRequest(engine, Post, path, token).asObject(newItem(34.56))
        with(BuiltRequest(engine, Get, "$path?occurrence=${item.id}", token).send<Occurrence>()) {
            val addedItems = response.content.parseResponse<OccurrencesResponse>()?.items
            response.status() shouldBe HttpStatusCode.OK
            addedItems?.size shouldBe 1
            addedItems?.first()!!::class.java shouldBe Occurrence::class.java
            addedItems.first().amount shouldBe "34.56"
        }
    }

    "verify getting an item that does not exist" {
        BuiltRequest(engine, Get, "$path?occurrence=99", token).sendStatus<Unit>() shouldBe HttpStatusCode.NotFound
    }

    "verify adding a new item" {
        with(BuiltRequest(engine, Post, path, token).send(newItem(45.67))) {
            val addedItem = response.content.parseResponse<Occurrence>()
            response.status() shouldBe HttpStatusCode.Created
            addedItem?.amount shouldBe "45.67"
        }
    }

    "verify adding a duplicate item" {
        val item = BuiltRequest(engine, Post, path, token).asObject(newItem(8))
        BuiltRequest(engine, Post, path, token).sendStatus((newItem(9, id = item.id))) shouldBe HttpStatusCode.Conflict
    }

    "verify updating an added item" {
        val item = BuiltRequest(engine, Post, path, token).asObject(newItem(34.56))
        val updatedItem = item.copy(
            amount = "77.77"
        )
        val updateRequest = BuiltRequest(engine, Put, path, token).send(updatedItem)

        with(updateRequest) {
            val addedItem = response.content.parseResponse<Occurrence>()
            val id = addedItem?.id
            val className = addedItem!!::class.java.simpleName
            val fields = addedItem.history!!.map { it.field }

            response.status() shouldBe HttpStatusCode.OK
            addedItem.amount shouldBe "77.77"
            fields[0] shouldBe "$className $id amount"
        }
    }

    "verify updating a non existent item" {
        BuiltRequest(engine, Put, path, token).sendStatus(newItem(5, 99)) shouldBe HttpStatusCode.BadRequest
    }

    "verify updating without an id adds a new item" {
        BuiltRequest(engine, Put, path, token).sendStatus(newItem(6)) shouldBe HttpStatusCode.Created
    }

    "verify deleting and item that has been added" {
        val addedItem =
            BuiltRequest(engine, Post, path, token).send(newItem(7)).response.content.parseResponse<Occurrence>()
        BuiltRequest(
            engine,
            Delete,
            "$path?occurrence=${addedItem?.id}",
            token
        ).sendStatus<Unit>() shouldBe HttpStatusCode.OK
    }

    "verify deleting item that doesn't exist" {
        BuiltRequest(engine, Delete, "$path?occurrence=99", token).sendStatus<Unit>() shouldBe HttpStatusCode.NotFound
    }
})
