package billMan

import BaseTest
import BuiltRequest
import categories.CategoriesResponse
import com.weesnerdevelopment.utils.Path
import io.kotlintest.shouldBe
import io.ktor.http.HttpMethod.Companion.Delete
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpMethod.Companion.Put
import io.ktor.http.HttpStatusCode
import parse
import shared.billMan.Category
import shared.fromJson

class CategoryTests : BaseTest({ token ->
    val categoryStart = "randomCategory"
    fun newItem(addition: Int, id: Int? = null) = Category(
        id = id,
        name = "$categoryStart$addition"
    )

    val path = Path.BillMan.categories

    "verify getting base url returns ok" {
        BuiltRequest(engine, Get, path, token).sendStatus<Unit>() shouldBe HttpStatusCode.OK
    }

    "verify getting base url returns all items in table" {
        BuiltRequest(engine, Post, path, token).send(newItem(0))
        BuiltRequest(engine, Post, path, token).send(newItem(1))
        with(BuiltRequest(engine, Get, path, token).send<Unit>()) {
            val responseItems = response.content?.fromJson<CategoriesResponse>()?.items
            val item1 = responseItems!![responseItems.lastIndex - 1]
            val item2 = responseItems[responseItems.lastIndex]
            response.status() shouldBe HttpStatusCode.OK
            item1.name shouldBe "${categoryStart}0"
            item2.name shouldBe "${categoryStart}1"
        }
    }

    "verify getting an added item" {
        val item = BuiltRequest(engine, Post, path, token).asObject(newItem(2))
        with(BuiltRequest(engine, Get, "$path?category=${item.id}", token).send<Category>()) {
            val addedItems = response.content.parse<CategoriesResponse>().items
            response.status() shouldBe HttpStatusCode.OK
            addedItems?.size shouldBe 1
            addedItems?.first()?.name shouldBe "${categoryStart}2"
        }
    }

    "verify getting an item that does not exist" {
        BuiltRequest(engine, Get, "$path?category=99", token).sendStatus<Unit>() shouldBe HttpStatusCode.NotFound
    }

    "verify adding a new item" {
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(3)) shouldBe HttpStatusCode.Created
    }

    "verify adding a duplicate item" {
        BuiltRequest(engine, Post, path, token).send(newItem(8))
        BuiltRequest(engine, Post, path, token).sendStatus((newItem(8))) shouldBe HttpStatusCode.Conflict
    }

    "verify updating an added item" {
        val updatedName = "cat4"
        val category = BuiltRequest(engine, Post, path, token).asObject(newItem(4))
        val updateRequest = BuiltRequest(engine, Put, path, token).send(category.copy(name = updatedName))

        with(updateRequest) {
            val addedItem = response.content?.fromJson<Category>()
            response.status() shouldBe HttpStatusCode.OK
            addedItem?.name shouldBe updatedName
            addedItem?.history?.get(0)?.field shouldBe "${addedItem!!::class.java.simpleName} ${addedItem.id} name"
        }
    }

    "verify updating a non existent item" {
        BuiltRequest(engine, Put, path, token).sendStatus(newItem(5, 99)) shouldBe HttpStatusCode.BadRequest
    }

    "verify updating without an id adds a new item" {
        BuiltRequest(engine, Put, path, token).sendStatus(newItem(6)) shouldBe HttpStatusCode.Created
    }

    "verify deleting and item that has been added" {
        val addedItem = BuiltRequest(engine, Post, path, token).send(newItem(7)).response.content?.fromJson<Category>()
        BuiltRequest(
            engine,
            Delete,
            "$path?category=${addedItem?.id}",
            token
        ).sendStatus<Unit>() shouldBe HttpStatusCode.OK
    }

    "verify deleting item that doesn't exist" {
        BuiltRequest(engine, Delete, "$path?category=99", token).sendStatus<Unit>() shouldBe HttpStatusCode.NotFound
    }
})
