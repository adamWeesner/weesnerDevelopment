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
import occurrences.OccurrencesResponse
import parse
import shared.auth.User
import shared.base.History
import shared.billMan.*
import shared.fromJson
import shared.taxFetcher.PayPeriod
import java.util.*

class OccurrenceTests : BaseTest({ token ->
    val signedInUser = BuiltRequest(engine, Get, "${Path.User.base}${Path.User.account}", token).asObject<User>()

    val startCategory = BuiltRequest(engine, Post, Path.BillMan.categories, token).asObject(
        Category(name = "randomCategory")
    )

    val startBill = BuiltRequest(engine, Post, Path.BillMan.bills, token).asObject(
        Bill(
            owner = signedInUser,
            name = "billStart",
            amount = "1.23",
            categories = listOf(startCategory),
            color = Color(red = 255, green = 255, blue = 255, alpha = 255)
        )
    )

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
        BuiltRequest(engine, Get, path, token).sendStatus<Unit>() shouldBe HttpStatusCode.BadRequest
    }

    "verify getting base url returns ok" {
        BuiltRequest(engine, Get, "$path?bill=0", token).sendStatus<Unit>() shouldBe HttpStatusCode.OK
    }

    "verify getting base url returns all items in table" {
        val addedItem = BuiltRequest(engine, Post, path, token).asObject(newItem(12.34))
        BuiltRequest(engine, Post, path, token).send(newItem(23.45))
        with(BuiltRequest(engine, Get, "$path?bill=${addedItem.itemId}", token).send<Unit>()) {
            val responseItems = response.content?.parse<OccurrencesResponse>()?.items
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
        with(BuiltRequest(engine, Get, "$path/${item.id}", token).send<Occurrence>()) {
            val addedItem = response.content.parse<Occurrence>()
            response.status() shouldBe HttpStatusCode.OK
            addedItem::class.java shouldBe Occurrence::class.java
            addedItem.amount shouldBe "34.56"
        }
    }

    "verify getting an item that does not exist" {
        BuiltRequest(engine, Get, "$path/99", token).sendStatus<Unit>() shouldBe HttpStatusCode.NotFound
    }

    "verify adding a new item" {
        with(BuiltRequest(engine, Post, path, token).send(newItem(45.67))) {
            val addedItem = response.content.parse<Occurrence>()
            response.status() shouldBe HttpStatusCode.Created
            addedItem.amount shouldBe "45.67"
        }
    }

    "verify adding a duplicate item" {
        val item = BuiltRequest(engine, Post, path, token).asObject(newItem(8))
        BuiltRequest(engine, Post, path, token).sendStatus((newItem(9, id = item.id))) shouldBe HttpStatusCode.Conflict
    }

    "verify updating an added item" {
        val item = BuiltRequest(engine, Post, path, token).asObject(newItem(34.56))
        val updatedItem = item.copy(
            amount = "77.77",
            amountLeft = "77.77",
            sharedUsers = listOf(signedInUser),
            payments = listOf(
                Payment(
                    owner = signedInUser,
                    amount = "66.66"
                )
            )
        )
        val updateRequest = BuiltRequest(engine, Put, path, token).send(updatedItem)

        with(updateRequest) {
            val addedItem = response.content.parse<Occurrence>()
            val id = addedItem.id
            val className = addedItem::class.java.simpleName
            val fields = addedItem.history!!.map { it.field }

            response.status() shouldBe HttpStatusCode.OK
            addedItem.amount shouldBe "77.77"
            addedItem.amountLeft shouldBe "11.11"
            addedItem.payments?.size shouldBe 1
            fields[0] shouldBe "$className $id amount"
            fields[1] shouldBe "$className $id amountLeft"
            fields[2] shouldBe "$className $id sharedUser"
            fields[3] shouldBe "${Payment::class.java.simpleName} ${addedItem.payments?.first()?.id} amount"
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
            BuiltRequest(engine, Post, path, token).send(newItem(7)).response.content?.fromJson<Occurrence>()
        BuiltRequest(engine, Delete, "$path/${addedItem?.id}", token).sendStatus<Unit>() shouldBe HttpStatusCode.OK
    }

    "verify deleting item that doesn't exist" {
        BuiltRequest(engine, Delete, "$path/99", token).sendStatus<Unit>() shouldBe HttpStatusCode.NotFound
    }

    "verify cannot add payment more than amount left" {
        val item = BuiltRequest(engine, Post, path, token).asObject(newItem(45.67))
        val updatedItem = item.copy(
            payments = listOf(
                Payment(
                    owner = signedInUser,
                    amount = "60"
                )
            )
        )
        BuiltRequest(engine, Put, path, token).sendStatus(updatedItem) shouldBe HttpStatusCode.BadRequest
    }
})
