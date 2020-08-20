package billMan

import BaseTest
import HttpLog
import Path.BillMan
import io.ktor.http.HttpMethod.Companion.Delete
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpMethod.Companion.Put
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.util.KtorExperimentalAPI
import logging.LoggingResponse
import org.junit.jupiter.api.*
import shared.billMan.Category
import shared.billMan.responses.CategoriesResponse
import shouldBe
import shouldBeAtLeast

@KtorExperimentalAPI
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DbLoggerTests : BaseTest() {
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
}