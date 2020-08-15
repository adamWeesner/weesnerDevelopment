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
import shared.billMan.Color
import shared.billMan.Income
import shared.billMan.responses.IncomeResponse

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
        BuiltRequest(engine, Get, path, token).sendStatus<Unit>() shouldBe NoContent
    }

    "verify getting base url returns all items in table" {
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(0)) shouldBe Created
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(1)) shouldBe Created
        with(BuiltRequest(engine, Get, path, token).send<Unit>()) {
            val responseItems = response.content.parseResponse<IncomeResponse>()?.items
            val item1 = responseItems!![responseItems.lastIndex - 1]
            val item2 = responseItems[responseItems.lastIndex]
            response.status() shouldBe OK
            item1.name shouldBe "${billStart}0"
            item2.name shouldBe "${billStart}1"
        }
    }

    "verify getting an added item" {
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(2)) shouldBe Created

        with(BuiltRequest(engine, Get, "$path?id=3", token).send<Income>()) {
            val addedItems = response.content.parseResponse<IncomeResponse>()?.items
            response.status() shouldBe OK
            addedItems?.size shouldBe 1
            addedItems?.first()!!::class.java shouldBe Income::class.java
            addedItems.first().name shouldBe "${billStart}2"
        }
    }

    "verify getting an item that does not exist" {
        BuiltRequest(engine, Get, "$path?id=99", token).sendStatus<Unit>() shouldBe NoContent
    }

    "verify adding a new item" {
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(3)) shouldBe Created

        with(BuiltRequest(engine, Get, "$path?id=4", token).send<Income>()) {
            val addedItem = response.content.parseResponse<IncomeResponse>()?.items?.first()
            response.status() shouldBe OK
            addedItem?.name shouldBe "${billStart}3"
        }
    }

    "verify updating an added item" {
        val updatedName = "income4"
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(4)) shouldBe Created
        val income = BuiltRequest(engine, Get, "$path?id=5", token).asObject<IncomeResponse>().items?.first()!!

        BuiltRequest(engine, Put, path, token).sendStatus(income.copy(name = updatedName)) shouldBe OK

        with(BuiltRequest(engine, Get, "$path?id=5", token).send<Unit>()) {
            val addedItem = response.content.parseResponse<IncomeResponse>()?.items?.first()
            response.status() shouldBe OK
            addedItem?.name shouldBe updatedName
            addedItem?.history?.get(0)?.field shouldBe "${addedItem!!::class.java.simpleName} ${addedItem.id} name"
        }
    }

    "verify updating a non existent item" {
        BuiltRequest(engine, Put, path, token).sendStatus(newItem(5, 99)) shouldBe BadRequest
    }

    "verify updating without an id " {
        BuiltRequest(engine, Put, path, token).sendStatus(newItem(6)) shouldBe BadRequest
    }

    "verify deleting and item that has been added" {
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(7)) shouldBe Created
        BuiltRequest(engine, Delete, "$path?id=6", token).send<Unit>().apply {
            response.status() shouldBe OK
            response.content.parseResponse<Any>()!!::class.java shouldBe String::class.java
        }
    }

    "verify deleting item that doesn't exist" {
        BuiltRequest(engine, Delete, "$path?id=99", token).sendStatus<Unit>() shouldBe NotFound
    }
})
