package com.weesnerdevelopment.billMan

import Path.BillMan
import com.weesnerdevelopment.billman.income.IncomeTable
import com.weesnerdevelopment.billman.incomeOccurrences.IncomeOccurrencesTable
import com.weesnerdevelopment.shared.auth.User
import com.weesnerdevelopment.shared.billMan.Color
import com.weesnerdevelopment.shared.billMan.Income
import com.weesnerdevelopment.shared.billMan.responses.IncomeResponse
import com.weesnerdevelopment.test.utils.BaseTest
import com.weesnerdevelopment.test.utils.parseResponse
import com.weesnerdevelopment.test.utils.shouldBe
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

class IncomeTests : BaseTest("application-test.conf") {
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
        get(path).sendStatus<Unit>() shouldBe NoContent
    }

    @Test
    @Order(2)
    fun `verify getting base url returns all items in table`() {
        post(path).sendStatus(newItem()) shouldBe Created
        post(path).sendStatus(newItem()) shouldBe Created

        val request = get(path).send<Unit>()
        val responseItems = request.response.content.parseResponse<IncomeResponse>()?.items

        val item1 = responseItems!![responseItems.lastIndex - 1]
        val item2 = responseItems[responseItems.lastIndex]
        request.response.status() shouldBe OK
        item1.name shouldBe "${incomeStart}1"
        item2.name shouldBe "${incomeStart}2"
    }

    @Test
    @Order(3)
    fun `verify getting an added item`() {
        post(path).sendStatus(newItem()) shouldBe Created
        get(path).asObject<IncomeResponse>().items

        val request = get(path, 3).send<Income>()
        val addedItems = request.response.content.parseResponse<IncomeResponse>()?.items

        request.response.status() shouldBe OK
        addedItems?.size shouldBe 1
        addedItems?.first()!!::class.java shouldBe Income::class.java
        addedItems.first().name shouldBe "${incomeStart}3"
    }

    @Test
    @Order(4)
    fun `verify getting an item that does not exist`() {
        get(path, 99).sendStatus<Unit>() shouldBe NoContent
    }

    @Test
    @Order(5)
    fun `verify adding a new item`() {
        post(path).sendStatus(newItem()) shouldBe Created

        val request = get(path, 4).send<Income>()
        val addedItem = request.response.content.parseResponse<IncomeResponse>()?.items?.first()

        request.response.status() shouldBe OK
        addedItem?.name shouldBe "${incomeStart}4"
    }

    @Test
    @Order(6)
    fun `verify updating an added item`() {
        val updatedName = "income4"
        post(path).sendStatus(newItem()) shouldBe Created
        val income = get(path, 5).asObject<IncomeResponse>().items?.first()!!
        put(path).sendStatus(income.copy(name = updatedName)) shouldBe OK

        val request = get(path, 5).send<Unit>()
        val addedItem = request.response.content.parseResponse<IncomeResponse>()?.items?.first()

        request.response.status() shouldBe OK
        addedItem?.name shouldBe updatedName
        addedItem?.history?.get(0)?.field shouldBe "${addedItem!!::class.java.simpleName} ${addedItem.id} name"
    }

    @Test
    @Order(7)
    fun `verify updating a non existent item`() {
        put(path).sendStatus(newItem(id = 99)) shouldBe BadRequest
    }

    @Test
    @Order(8)
    fun `verify updating without an id`() {
        put(path).sendStatus(newItem()) shouldBe BadRequest
    }

    @Test
    @Order(9)
    fun `verify deleting and item that has been added`() {
        post(path).sendStatus(newItem()) shouldBe Created
        delete(path, 6).sendStatus<Unit>() shouldBe OK
    }

    @Test
    @Order(10)
    fun `verify deleting item that doesn't exist`() {
        delete(path, 99).sendStatus<Unit>() shouldBe NotFound
    }
}
