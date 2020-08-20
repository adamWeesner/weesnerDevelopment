package billMan

import BaseTest
import BuiltRequest
import Path
import io.ktor.http.HttpMethod.Companion.Delete
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpMethod.Companion.Put
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Conflict
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.NoContent
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.util.KtorExperimentalAPI
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import parseResponse
import shared.billMan.Category
import shared.billMan.responses.CategoriesResponse
import shouldBe
import shouldNotBe

@KtorExperimentalAPI
class CategoryTests : BaseTest() {
    private val categoryStart = "randomCategory"
    fun newItem(addition: Int, id: Int? = null) = Category(id = id, name = "$categoryStart$addition")

    val path = Path.BillMan.categories

    @Test
    @Order(1)
    fun `verify getting base url`() {
        BuiltRequest(engine, Get, path, token).sendStatus<Unit>() shouldBe NoContent
    }

    @Test
    @Order(2)
    fun `verify getting base url returns all items in table`() {
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(0)) shouldBe Created
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(1)) shouldBe Created

        with(BuiltRequest(engine, Get, path, token).send<Unit>()) {
            val responseItems = response.content.parseResponse<CategoriesResponse>()?.items
            val item1 = responseItems!![responseItems.lastIndex - 1]
            val item2 = responseItems[responseItems.lastIndex]
            response.status() shouldBe OK
            item1.name shouldBe "${categoryStart}0"
            item2.name shouldBe "${categoryStart}1"
        }
    }

    @Test
    @Order(3)
    fun `verify getting an added item`() {
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(2)) shouldBe Created

        with(BuiltRequest(engine, Get, path, token).send<Category>()) {
            val addedItems = response.content.parseResponse<CategoriesResponse>()?.items?.last()
            response.status() shouldBe OK
            addedItems?.name shouldBe "${categoryStart}2"
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
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(3)) shouldBe Created
    }

    @Test
    @Order(6)
    fun `verify adding a duplicate item`() {
        BuiltRequest(engine, Post, path, token).send(newItem(8))
        BuiltRequest(engine, Post, path, token).sendStatus((newItem(8))) shouldBe Conflict
    }

    @Test
    @Order(7)
    fun `verify updating an added item`() {
        val updatedName = "cat4"
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(4)) shouldBe Created

        val category = BuiltRequest(engine, Get, path, token)
            .asObject<CategoriesResponse>().items?.last()

        BuiltRequest(engine, Put, path, token).sendStatus(
            category?.copy(
                name = updatedName,
                owner = signedInUser
            )
        ) shouldBe OK

        val updatedCategory = BuiltRequest(engine, Get, "$path?id=${category?.id}", token)
            .asObject<CategoriesResponse>().items?.first()

        with(updatedCategory) {
            updatedCategory shouldNotBe null
            this!!.name shouldBe updatedName
            this.history?.get(0)?.field shouldBe "${this::class.java.simpleName} ${this.id} name"
        }
    }

    @Test
    @Order(8)
    fun `verify updating a non existent item`() {
        BuiltRequest(engine, Put, path, token).sendStatus(newItem(5, 99)) shouldBe BadRequest
    }

    @Test
    @Order(9)
    fun `verify updating without an id`() {
        BuiltRequest(engine, Put, path, token).sendStatus(newItem(6)) shouldBe BadRequest
    }

    @Test
    @Order(10)
    fun `verify deleting and item that has been added`() {
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(7)) shouldBe Created

        val addedItem = BuiltRequest(engine, Get, path, token).asObject<CategoriesResponse>().items?.last()

        BuiltRequest(engine, Delete, "$path?id=${addedItem?.id}", token).sendStatus<Unit>() shouldBe OK
    }

    @Test
    @Order(11)
    fun `verify deleting item that doesn't exist`() {
        BuiltRequest(engine, Delete, "$path?id=99", token).sendStatus<Unit>() shouldBe NotFound
    }
}
