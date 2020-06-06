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
import parse
import shared.auth.User
import shared.billMan.Bill
import shared.billMan.Category
import shared.billMan.Color
import shared.billMan.responses.BillsResponse
import shared.fromJson

class BillTests : BaseTest({ token ->
    val billStart = "randomBill"
    val signedInUser =
        BuiltRequest(engine, Get, "${Path.User.base}${Path.User.account}", token).asObject<User>()

    val startCategory = BuiltRequest(engine, Post, Path.BillMan.categories, token).asObject(
        Category(name = "randomCategory")
    )

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
    )

    val path = Path.BillMan.bills

    "verify getting base url returns ok" {
        BuiltRequest(engine, Get, path, token).sendStatus<Unit>() shouldBe HttpStatusCode.OK
    }

    "verify getting base url returns all items in table" {
        BuiltRequest(engine, Post, path, token).send(newItem(0))
        BuiltRequest(engine, Post, path, token).send(newItem(1))
        with(BuiltRequest(engine, Get, path, token).send<Unit>()) {
            val responseItems = response.content?.fromJson<BillsResponse>()?.items
            val item1 = responseItems!![responseItems.lastIndex - 1]
            val item2 = responseItems[responseItems.lastIndex]
            response.status() shouldBe HttpStatusCode.OK
            item1.name shouldBe "${billStart}0"
            item2.name shouldBe "${billStart}1"
        }
    }

    "verify getting an added item" {
        val item = BuiltRequest(engine, Post, path, token).asObject(newItem(2))
        with(BuiltRequest(engine, Get, "$path?bill=${item.id}", token).send<Bill>()) {
            val addedItems = response.content.parse<BillsResponse>().items
            response.status() shouldBe HttpStatusCode.OK
            addedItems?.size shouldBe 1
            addedItems?.first()!!::class.java shouldBe Bill::class.java
            addedItems.first().name shouldBe "${billStart}2"
        }
    }

    "verify getting an item that does not exist" {
        BuiltRequest(engine, Get, "$path?bill=99", token).sendStatus<Unit>() shouldBe HttpStatusCode.NotFound
    }

    "verify adding a new item" {
        with(BuiltRequest(engine, Post, path, token).send(newItem(3))) {
            val addedItem = response.content.parse<Bill>()
            response.status() shouldBe HttpStatusCode.Created
            addedItem.name shouldBe "${billStart}3"
            addedItem.color.copy(dateUpdated = -1, dateCreated = -1) shouldBe Color(
                addedItem.color.id,
                255,
                255,
                255,
                255,
                listOf()
            ).copy(dateUpdated = -1, dateCreated = -1)
        }
    }

    "verify adding a duplicate item" {
        val item = BuiltRequest(engine, Post, path, token).asObject(newItem(8))
        BuiltRequest(engine, Post, path, token).sendStatus((newItem(9, item.id))) shouldBe HttpStatusCode.Conflict
    }

    "verify updating an added item" {
        val updatedName = "cat4"
        val bill = BuiltRequest(engine, Post, path, token).asObject(newItem(4))
        val updatedBill =
            bill.copy(
                name = updatedName,
                color = bill.color.copy(green = 150),
                sharedUsers = listOf(signedInUser)
            )
        val updateRequest = BuiltRequest(engine, Put, path, token).send(updatedBill)

        with(updateRequest) {
            val addedItem = response.content.parse<Bill>()
            val id = addedItem.id
            val className = addedItem::class.java.simpleName
            val fields = addedItem.history!!.map { it.field }

            response.status() shouldBe HttpStatusCode.OK
            addedItem.name shouldBe updatedName
            addedItem.color.green shouldBe 150
            fields[0] shouldBe "$className $id name"
            fields[1] shouldBe "$className $id sharedUser"
            fields[2] shouldBe "${Color::class.java.simpleName} ${addedItem.color.id} green"
        }
    }

    "verify updating a non existent item" {
        BuiltRequest(engine, Put, path, token).sendStatus(newItem(5, 99)) shouldBe HttpStatusCode.BadRequest
    }

    "verify updating without an id adds a new item" {
        BuiltRequest(engine, Put, path, token).sendStatus(newItem(6)) shouldBe HttpStatusCode.Created
    }

    "verify deleting and item that has been added" {
        val addedItem = BuiltRequest(engine, Post, path, token).send(newItem(7)).response.content?.fromJson<Bill>()
        BuiltRequest(engine, Delete, "$path?bill=${addedItem?.id}", token).sendStatus<Unit>() shouldBe HttpStatusCode.OK
    }

    "verify deleting item that doesn't exist" {
        BuiltRequest(engine, Delete, "$path?bill=99", token).sendStatus<Unit>() shouldBe HttpStatusCode.NotFound
    }
})
