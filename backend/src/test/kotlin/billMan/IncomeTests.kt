package billMan

import BaseTest
import BuiltRequest
import com.weesnerdevelopment.utils.Path
import income.IncomeResponse
import io.kotlintest.shouldBe
import io.ktor.http.HttpMethod.Companion.Delete
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpMethod.Companion.Put
import io.ktor.http.HttpStatusCode
import parse
import shared.auth.User
import shared.billMan.Color
import shared.billMan.Income

class IncomeTests : BaseTest({ token ->
    val billStart = "randomIncome"
    val signedInUser =
        BuiltRequest(engine, Get, "${Path.User.base}${Path.User.account}", token).asObject<User>()

    fun newItem(
        addition: Int,
        id: Int? = null,
        varyingAmount: Boolean = false,
        owner: User = signedInUser
    ) = Income(
        id = id,
        owner = owner,
        name = "$billStart$addition",
        amount = "1.23",
        varyingAmount = varyingAmount,
        color = Color(red = 255, green = 255, blue = 255, alpha = 255)
    )

    val path = Path.BillMan.income

    "verify getting base url returns ok" {
        BuiltRequest(engine, Get, path, token).sendStatus<Unit>() shouldBe HttpStatusCode.OK
    }

    "verify getting base url returns all items in table" {
        BuiltRequest(engine, Post, path, token).send(newItem(0))
        BuiltRequest(engine, Post, path, token).send(newItem(1))
        with(BuiltRequest(engine, Get, path, token).send<Unit>()) {
            val responseItems = response.content.parse<IncomeResponse>().items
            val item1 = responseItems!![responseItems.lastIndex - 1]
            val item2 = responseItems[responseItems.lastIndex]
            response.status() shouldBe HttpStatusCode.OK
            item1.name shouldBe "${billStart}0"
            item2.name shouldBe "${billStart}1"
        }
    }

    "verify getting an added item" {
        val item = BuiltRequest(engine, Post, path, token).asObject(newItem(2))
        with(BuiltRequest(engine, Get, "$path/${item.id}", token).send<Income>()) {
            val addedItem = response.content.parse<Income>()
            response.status() shouldBe HttpStatusCode.OK
            addedItem::class.java shouldBe Income::class.java
            addedItem.name shouldBe "${billStart}2"
        }
    }

    "verify getting an item that does not exist" {
        BuiltRequest(engine, Get, "$path/99", token).sendStatus<Unit>() shouldBe HttpStatusCode.NotFound
    }

    "verify adding a new item" {
        with(BuiltRequest(engine, Post, path, token).send(newItem(3))) {
            val addedItem = response.content.parse<Income>()
            response.status() shouldBe HttpStatusCode.Created
            addedItem.name shouldBe "${billStart}3"
        }
    }

    "verify adding a duplicate item" {
        val item = BuiltRequest(engine, Post, path, token).asObject(newItem(8))
        BuiltRequest(engine, Post, path, token).sendStatus((newItem(9, item.id))) shouldBe HttpStatusCode.Conflict
    }

    "verify updating an added item" {
        val updatedName = "income4"
        val income = BuiltRequest(engine, Post, path, token).asObject(newItem(4))
        val updateRequest = BuiltRequest(engine, Put, path, token).send(income.copy(name = updatedName))

        with(updateRequest) {
            val addedItem = response.content.parse<Income>()
            response.status() shouldBe HttpStatusCode.OK
            addedItem.name shouldBe updatedName
            addedItem.history?.get(0)?.field shouldBe "${addedItem::class.java.simpleName} ${addedItem.id} name"
        }
    }

    "verify updating a non existent item" {
        BuiltRequest(engine, Put, path, token).sendStatus(newItem(5, 99)) shouldBe HttpStatusCode.BadRequest
    }

    "verify updating without an id adds a new item" {
        BuiltRequest(engine, Put, path, token).sendStatus(newItem(6)) shouldBe HttpStatusCode.Created
    }

    "verify deleting and item that has been added" {
        val addedItem = BuiltRequest(engine, Post, path, token).send(newItem(7)).response.content.parse<Income>()
        BuiltRequest(engine, Delete, "$path/${addedItem.id}", token).sendStatus<Unit>() shouldBe HttpStatusCode.OK
    }

    "verify deleting item that doesn't exist" {
        BuiltRequest(engine, Delete, "$path/99", token).sendStatus<Unit>() shouldBe HttpStatusCode.NotFound
    }
})
