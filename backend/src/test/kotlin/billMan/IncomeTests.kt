package billMan

import BaseTest
import BuiltRequest
import com.weesnerdevelopment.utils.Path
import com.weesnerdevelopment.utils.Path.BillMan
import income.IncomeTable
import incomeOccurrences.IncomeOccurrencesTable
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
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import parseResponse
import shared.auth.User
import shared.billMan.Color
import shared.billMan.Income
import shared.billMan.responses.IncomeResponse

class IncomeTests : BaseTest({ token ->
    transaction {
        SchemaUtils.drop(IncomeOccurrencesTable, IncomeTable)
        SchemaUtils.create(IncomeOccurrencesTable, IncomeTable)
    }

    val path = BillMan.income
    var counter = 1

    val incomeStart = "randomIncome"
    val signedInUser =
        BuiltRequest(engine, Get, "${Path.User.base}${Path.User.account}", token).asObject<User>()

    fun newItem(
        addition: Int = counter,
        id: Int? = null,
        varyingAmount: Boolean = false,
        owner: User = signedInUser
    ) = Income(
        id = id,
        owner = owner,
        name = "$incomeStart$addition",
        amount = "1.23",
        varyingAmount = varyingAmount,
        color = Color(red = 255, green = 255, blue = 255, alpha = 255)
    ).also {
        println("added name ${it.name}")
        counter++
    }

    "verify getting base url returns ok" {
        BuiltRequest(engine, Get, path, token).sendStatus<Unit>() shouldBe NoContent
    }

    "verify getting base url returns all items in table" {
        BuiltRequest(engine, Post, path, token).sendStatus(newItem()) shouldBe Created
        BuiltRequest(engine, Post, path, token).sendStatus(newItem()) shouldBe Created
        with(BuiltRequest(engine, Get, path, token).send<Unit>()) {
            val responseItems = response.content.parseResponse<IncomeResponse>()?.items
            val item1 = responseItems!![responseItems.lastIndex - 1]
            val item2 = responseItems[responseItems.lastIndex]
            response.status() shouldBe OK
            item1.name shouldBe "${incomeStart}1"
            item2.name shouldBe "${incomeStart}2"
        }
    }

    "verify getting an added item" {
        BuiltRequest(engine, Post, path, token).sendStatus(newItem()) shouldBe Created

        BuiltRequest(engine, Get, path, token).asObject<IncomeResponse>().items?.forEach { println("item $it") }

        with(BuiltRequest(engine, Get, "$path?id=3", token).send<Income>()) {
            val addedItems = response.content.parseResponse<IncomeResponse>()?.items
            response.status() shouldBe OK
            addedItems?.size shouldBe 1
            addedItems?.first()!!::class.java shouldBe Income::class.java
            addedItems.first().name shouldBe "${incomeStart}3"
        }
    }

    "verify getting an item that does not exist" {
        BuiltRequest(engine, Get, "$path?id=99", token).sendStatus<Unit>() shouldBe NoContent
    }

    "verify adding a new item" {
        BuiltRequest(engine, Post, path, token).sendStatus(newItem()) shouldBe Created

        with(BuiltRequest(engine, Get, "$path?id=4", token).send<Income>()) {
            val addedItem = response.content.parseResponse<IncomeResponse>()?.items?.first()
            response.status() shouldBe OK
            addedItem?.name shouldBe "${incomeStart}4"
        }
    }

    "verify updating an added item" {
        val updatedName = "income4"
        BuiltRequest(engine, Post, path, token).sendStatus(newItem()) shouldBe Created
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
        BuiltRequest(engine, Put, path, token).sendStatus(newItem(id = 99)) shouldBe BadRequest
    }

    "verify updating without an id " {
        BuiltRequest(engine, Put, path, token).sendStatus(newItem()) shouldBe BadRequest
    }

    "verify deleting and item that has been added" {
        BuiltRequest(engine, Post, path, token).sendStatus(newItem()) shouldBe Created
        BuiltRequest(engine, Delete, "$path?id=6", token).send<Unit>().apply {
            response.status() shouldBe OK
            response.content.parseResponse<Any>()!!::class.java shouldBe String::class.java
        }
    }

    "verify deleting item that doesn't exist" {
        BuiltRequest(engine, Delete, "$path?id=99", token).sendStatus<Unit>() shouldBe NotFound
    }
})
