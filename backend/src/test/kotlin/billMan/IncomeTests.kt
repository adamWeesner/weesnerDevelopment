package billMan

import BaseTest
import BuiltRequest
import Path.BillMan
import income.IncomeTable
import incomeOccurrences.IncomeOccurrencesTable
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
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import parseResponse
import shared.auth.User
import shared.billMan.Color
import shared.billMan.Income
import shared.billMan.responses.IncomeResponse
import shouldBe

class IncomeTests : BaseTest() {
    val path = BillMan.income
    var counter = 1

    val incomeStart = "randomIncome"

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
        counter++
    }

    @BeforeAll
    fun start() {
        transaction {
            SchemaUtils.drop(IncomeOccurrencesTable, IncomeTable)
            SchemaUtils.create(IncomeOccurrencesTable, IncomeTable)
        }
    }

    @Test
    @Order(1)
    fun `verify getting base url returns ok`() {
        BuiltRequest(engine, Get, path, token).sendStatus<Unit>() shouldBe NoContent
    }

    @Test
    @Order(2)
    fun `verify getting base url returns all items in table`() {
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

    @Test
    @Order(3)
    fun `verify getting an added item`() {
        BuiltRequest(engine, Post, path, token).sendStatus(newItem()) shouldBe Created

        BuiltRequest(engine, Get, path, token).asObject<IncomeResponse>().items

        with(BuiltRequest(engine, Get, "$path?id=3", token).send<Income>()) {
            val addedItems = response.content.parseResponse<IncomeResponse>()?.items
            response.status() shouldBe OK
            addedItems?.size shouldBe 1
            addedItems?.first()!!::class.java shouldBe Income::class.java
            addedItems.first().name shouldBe "${incomeStart}3"
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
        BuiltRequest(engine, Post, path, token).sendStatus(newItem()) shouldBe Created

        with(BuiltRequest(engine, Get, "$path?id=4", token).send<Income>()) {
            val addedItem = response.content.parseResponse<IncomeResponse>()?.items?.first()
            response.status() shouldBe OK
            addedItem?.name shouldBe "${incomeStart}4"
        }
    }

    @Test
    @Order(6)
    fun `verify updating an added item`() {
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

    @Test
    @Order(7)
    fun `verify updating a non existent item`() {
        BuiltRequest(engine, Put, path, token).sendStatus(newItem(id = 99)) shouldBe BadRequest
    }

    @Test
    @Order(8)
    fun `verify updating without an id`() {
        BuiltRequest(engine, Put, path, token).sendStatus(newItem()) shouldBe BadRequest
    }

    @Test
    @Order(9)
    fun `verify deleting and item that has been added`() {
        BuiltRequest(engine, Post, path, token).sendStatus(newItem()) shouldBe Created
        BuiltRequest(engine, Delete, "$path?id=6", token).send<Unit>().apply {
            response.status() shouldBe OK
            response.content.parseResponse<Any>()!!::class.java shouldBe String::class.java
        }
    }

    @Test
    @Order(10)
    fun `verify deleting item that doesn't exist`() {
        BuiltRequest(engine, Delete, "$path?id=99", token).sendStatus<Unit>() shouldBe NotFound
    }
}
