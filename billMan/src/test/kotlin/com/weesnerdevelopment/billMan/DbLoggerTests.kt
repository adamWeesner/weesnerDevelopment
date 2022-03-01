package com.weesnerdevelopment.billMan

import HttpLog
import Path.BillMan
import com.weesnerdevelopment.shared.billMan.Category
import com.weesnerdevelopment.shared.billMan.responses.CategoriesResponse
import com.weesnerdevelopment.test.utils.BaseTest
import com.weesnerdevelopment.test.utils.shouldBe
import com.weesnerdevelopment.test.utils.shouldBeAtLeast
import com.weesnerdevelopment.test.utils.shouldBeAtMost
import io.ktor.http.HttpMethod.Companion.Delete
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpMethod.Companion.Put
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.OK
import logging.LoggingResponse
import org.junit.jupiter.api.*

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DbLoggerTests : BaseTest("application-test.conf") {
    @Test
    @Order(1)
    fun `adding an item adds to logs`() {
        post(BillMan.categories).sendStatus(Category(name = "Db Logger Category")) shouldBe Created

        val loggingItems = get(BillMan.logging).asObject<LoggingResponse>().items!!.filter {
            it.log.contains(BillMan.categories)
        }

        loggingItems.size shouldBeAtLeast 1
        loggingItems.last().log shouldBe HttpLog(Post, BillMan.categories, Created).toString()
    }

    @Test
    @Order(2)
    fun `updating an item adds to logs`() {
        val category = get(BillMan.categories).asObject<CategoriesResponse>().items?.last()!!
        put(BillMan.categories, category.id)
            .sendStatus(category.copy(name = "Db Logger Category v2", owner = signedInUser)) shouldBe OK

        val loggingItems = get(BillMan.logging).asObject<LoggingResponse>().items!!

        loggingItems.size shouldBeAtLeast 1
        loggingItems.last {
            it.log.contains(BillMan.categories) && it.log.contains(Put.value)
        }.log shouldBe HttpLog(Put, "${BillMan.categories}?id=${category.id}", OK).toString()
    }

    @Test
    @Order(3)
    fun `deleting an item adds to logs`() {
        val category = get(BillMan.categories).asObject<CategoriesResponse>().items?.last()!!
        delete(BillMan.categories, category.id).sendStatus<Unit>() shouldBe OK

        val loggingItems = get(BillMan.logging).asObject<LoggingResponse>().items!!

        loggingItems.size shouldBeAtLeast 1
        loggingItems.last {
            it.log.contains(BillMan.categories) && it.log.contains(Delete.value)
        }.log shouldBe HttpLog(Delete, "${BillMan.categories}?id=${category.id}", OK).toString()
    }

    @Test
    @Order(4)
    fun `verify hitting logging endpoint does not add logs to db`() {
        get(BillMan.logging).sendStatus<Unit>() shouldBe OK
        get(BillMan.logging).sendStatus<Unit>() shouldBe OK
        val loggingItems = get(BillMan.logging).asObject<LoggingResponse>().items!!

        loggingItems.filter {
            it.log.contains(BillMan.logging)
        }.size shouldBeAtMost 0
    }
}