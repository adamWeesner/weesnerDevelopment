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
import shared.auth.User
import shared.base.History
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
            item1 shouldBe Category(
                item1.id,
                null,
                "${categoryStart}0",
                listOf(),
                item1.dateCreated,
                item1.dateUpdated
            )
            item2 shouldBe Category(
                item2.id,
                null,
                "${categoryStart}1",
                listOf(),
                item2.dateCreated,
                item2.dateUpdated
            )
        }
    }

    "verify getting an added item" {
        val item = BuiltRequest(engine, Post, path, token).asObject(newItem(2))
        with(BuiltRequest(engine, Get, "$path/${item?.id}", token).send<Category>()) {
            val addedItem = response.content!!.fromJson<Category>()!!
            response.status() shouldBe HttpStatusCode.OK
            addedItem shouldBe Category(
                item?.id,
                null,
                "${categoryStart}2",
                listOf(),
                addedItem.dateCreated,
                addedItem.dateUpdated
            )
        }
    }

    "verify getting an item that does not exist" {
        BuiltRequest(engine, Get, "$path/99", token).sendStatus<Unit>() shouldBe HttpStatusCode.NotFound
    }

    "verify adding a new item" {
        with(BuiltRequest(engine, Post, path, token).send(newItem(3))) {
            val addedItem = response.content?.fromJson<Category>()!!
            response.status() shouldBe HttpStatusCode.Created
            addedItem shouldBe Category(
                addedItem.id,
                null,
                "${categoryStart}3",
                listOf(),
                addedItem.dateCreated,
                addedItem.dateUpdated
            )
        }
    }

    "verify adding a duplicate item" {
        BuiltRequest(engine, Post, path, token).send(newItem(8))
        BuiltRequest(engine, Post, path, token).sendStatus((newItem(8))) shouldBe HttpStatusCode.Conflict
    }

    "verify updating an added item" {
        val updatedName = "cat4"
        val userAccount = BuiltRequest(engine, Get, "${Path.User.base}${Path.User.account}", token).asObject<User>()
        val category = BuiltRequest(engine, Post, path, token).asObject(newItem(4))
        val updateRequest = BuiltRequest(engine, Put, path, token).send(category?.copy(name = updatedName))

        with(updateRequest) {
            val addedItem = response.content?.fromJson<Category>()
            response.status() shouldBe HttpStatusCode.OK
            addedItem shouldBe Category(
                category?.id,
                null,
                updatedName,
                listOf(
                    History(
                        addedItem?.history?.firstOrNull()?.id,
                        "${addedItem!!::class.java.simpleName} ${addedItem.id} name",
                        "${categoryStart}4",
                        updatedName,
                        userAccount!!,
                        addedItem.history?.firstOrNull()?.dateCreated ?: 0,
                        addedItem.history?.firstOrNull()?.dateUpdated ?: 0
                    )
                ),
                addedItem.dateCreated,
                addedItem.dateUpdated
            )
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
        BuiltRequest(engine, Delete, "$path/${addedItem?.id}", token).sendStatus<Unit>() shouldBe HttpStatusCode.OK
    }

    "verify deleting item that doesn't exist" {
        BuiltRequest(engine, Delete, "$path/99", token).sendStatus<Unit>() shouldBe HttpStatusCode.NotFound
    }
})
