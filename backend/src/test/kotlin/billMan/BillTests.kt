package billMan

import BaseTest
import BuiltRequest
import com.weesnerdevelopment.utils.Path
import io.kotlintest.shouldBe
import io.ktor.http.HttpMethod.Companion.Delete
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpMethod.Companion.Put
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.NoContent
import io.ktor.http.HttpStatusCode.Companion.OK
import parseResponse
import shared.auth.User
import shared.billMan.Bill
import shared.billMan.Category
import shared.billMan.Color
import shared.billMan.responses.BillsResponse
import shared.billMan.responses.CategoriesResponse

class BillTests : BaseTest({ token ->
    val billStart = "randomBill"
    val signedInUser =
        BuiltRequest(engine, Get, "${Path.User.base}${Path.User.account}", token).asObject<User>()

    BuiltRequest(engine, Post, Path.BillMan.categories, token).send(Category(name = "randomCategory"))

    val startCategory =
        BuiltRequest(engine, Get, Path.BillMan.categories, token).asObject<CategoriesResponse>().items?.first()!!

    fun newItem(
        addition: Int,
        id: Int? = null,
        varyingAmount: Boolean = false,
        payoffAmount: String? = null,
        sharedUsers: List<User>? = null,
        owner: User = signedInUser
    ) = Bill(
        id = id,
        owner = owner,
        name = "$billStart$addition",
        amount = "1.23",
        varyingAmount = varyingAmount,
        payoffAmount = payoffAmount,
        sharedUsers = sharedUsers,
        categories = listOf(startCategory),
        color = Color(red = 255, green = 255, blue = 255, alpha = 255)
    ).also(::println)

    val path = Path.BillMan.bills

    "verify getting base url returns ok" {
        BuiltRequest(engine, Get, path, token).sendStatus<Unit>() shouldBe NoContent
    }

    "verify getting base url returns all items in table" {
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(0)) shouldBe Created
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(1)) shouldBe Created

        with(BuiltRequest(engine, Get, path, token).send<Unit>()) {
            val responseItems = response.content.parseResponse<BillsResponse>()?.items
            val item1 = responseItems!![responseItems.lastIndex - 1]
            val item2 = responseItems[responseItems.lastIndex]
            response.status() shouldBe OK
            item1.name shouldBe "${billStart}0"
            item2.name shouldBe "${billStart}1"
        }
    }

    "verify getting an added item" {
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(2)) shouldBe Created
        with(BuiltRequest(engine, Get, "$path?id=3", token).send<Bill>()) {
            val addedItems = response.content.parseResponse<BillsResponse>()?.items
            response.status() shouldBe OK
            addedItems?.size shouldBe 1
            addedItems?.first()!!::class.java shouldBe Bill::class.java
            addedItems.first().name shouldBe "${billStart}2"
        }
    }

    "verify getting an item that does not exist" {
        BuiltRequest(engine, Get, "$path?id=99", token).sendStatus<Unit>() shouldBe NoContent
    }

    "verify adding a new item" {
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(3)) shouldBe Created
        with(BuiltRequest(engine, Get, "$path?id=4", token).send<Bill>()) {
            val addedItem = response.content.parseResponse<BillsResponse>()?.items?.first()
            response.status() shouldBe OK
            addedItem?.name shouldBe "${billStart}3"
            addedItem?.color?.copy(dateUpdated = -1, dateCreated = -1) shouldBe Color(
                addedItem?.color?.id,
                255,
                255,
                255,
                255,
                null
            ).copy(dateUpdated = -1, dateCreated = -1)
        }
    }

    "verify updating an added item" {
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(4)) shouldBe Created
        val bill = BuiltRequest(engine, Get, "$path?id=5", token).asObject<BillsResponse>().items?.first()
            .also { println("added $it") }
        val updatedName = "cat4"
        val updatedBill = bill?.copy(
            name = updatedName,
            color = bill.color.copy(green = 150),
            sharedUsers = listOf(signedInUser)
        )

        BuiltRequest(engine, Put, path, token).send(updatedBill)

        with(BuiltRequest(engine, Get, "$path?id=5", token).send<BillsResponse>()) {
            val addedItem =
                response.content.parseResponse<BillsResponse>()?.items?.first().also { println("updated $it") }
            val id = addedItem?.id
            val className = addedItem!!::class.java.simpleName
            val fields = addedItem.history!!.map { it.field }

            response.status() shouldBe OK
            addedItem.name shouldBe updatedName
            addedItem.color.green shouldBe 150
            fields[0] shouldBe "$className $id name"
            fields[1] shouldBe "$className $id sharedUser"
        }
    }

    "verify updating a non existent item" {
        BuiltRequest(engine, Put, path, token).sendStatus(newItem(5, 99)) shouldBe HttpStatusCode.BadRequest
    }

    "verify updating without an id" {
        BuiltRequest(engine, Put, path, token).sendStatus(newItem(6)) shouldBe BadRequest
    }

    "verify deleting and item that has been added" {
        BuiltRequest(engine, Post, path, token).sendStatus(newItem(7)) shouldBe Created
        BuiltRequest(engine, Delete, "$path?id=6", token).sendStatus<Unit>() shouldBe OK
    }

    "verify deleting item that doesn't exist" {
        BuiltRequest(engine, Delete, "$path?id=99", token).sendStatus<Unit>() shouldBe HttpStatusCode.NotFound
    }
})
