package billMan

import BaseTest
import Path
import billCategories.BillCategoriesTable
import billSharedUsers.BillsSharedUsersTable
import bills.BillsTable
import colors.ColorsTable
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.NoContent
import io.ktor.http.HttpStatusCode.Companion.OK
import occurrences.BillOccurrencesTable
import occurrencesSharedUsers.OccurrenceSharedUsersTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import parse
import parseResponse
import payments.PaymentsTable
import shared.auth.TokenResponse
import shared.auth.User
import shared.billMan.Bill
import shared.billMan.BillOccurrence
import shared.billMan.Category
import shared.billMan.Color
import shared.billMan.responses.BillsResponse
import shared.billMan.responses.CategoriesResponse
import shouldBe

class BillTests : BaseTest("application-test.conf") {
    val billStart = "randomBill"
    lateinit var startCategory: Category

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

    @BeforeAll
    fun start() {
        transaction {
            SchemaUtils.drop(
                BillsTable,
                ColorsTable,
                BillOccurrencesTable,
                BillCategoriesTable,
                BillsSharedUsersTable,
                PaymentsTable,
                OccurrenceSharedUsersTable
            )
            SchemaUtils.create(
                BillsTable,
                ColorsTable,
                BillOccurrencesTable,
                BillCategoriesTable,
                BillsSharedUsersTable,
                PaymentsTable,
                OccurrenceSharedUsersTable
            )
        }

        createUser()

        post(Path.BillMan.categories).send(Category(name = "randomCategory"))
        startCategory = get(Path.BillMan.categories).asObject<CategoriesResponse>().items?.last()!!
    }

    @Test
    @Order(1)
    fun `verify getting base url returns ok`() {
        get(path).sendStatus<Unit>() shouldBe NoContent
    }

    @Test
    @Order(2)
    fun `verify getting base url returns all items in table`() {
        post(path).sendStatus(newItem(0)) shouldBe Created
        post(path).sendStatus(newItem(1)) shouldBe Created

        val request = get(path).send<Unit>()
        val responseItems = request.response.content.parseResponse<BillsResponse>()?.items

        val item1 = responseItems!![responseItems.lastIndex - 1]
        val item2 = responseItems[responseItems.lastIndex]
        request.response.status() shouldBe OK
        item1.name shouldBe "${billStart}0"
        item2.name shouldBe "${billStart}1"
    }

    @Test
    @Order(3)
    fun `verify getting an added item`() {
        post(path).sendStatus(newItem(2)) shouldBe Created

        val request = get(path, 3).send<Bill>()
        val addedItems = request.response.content.parseResponse<BillsResponse>()?.items

        request.response.status() shouldBe OK
        addedItems?.size shouldBe 1
        addedItems?.first()!!::class.java shouldBe Bill::class.java
        addedItems.first().name shouldBe "${billStart}2"
    }

    @Test
    @Order(4)
    fun `verify getting an item that does not exist`() {
        get(path, 99).sendStatus<Unit>() shouldBe NoContent
    }

    @Test
    @Order(5)
    fun `verify adding a new item`() {
        post(path).sendStatus(newItem(3)) shouldBe Created

        val request = get(path, 4).send<Bill>()
        val addedItem = request.response.content.parseResponse<BillsResponse>()?.items?.first()

        request.response.status() shouldBe OK
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

    @Test
    @Order(6)
    fun `verify updating an added item`() {
        post(path).sendStatus(newItem(4)) shouldBe Created

        val bill = get(path, 5).asObject<BillsResponse>().items?.first()
        val updatedName = "cat4"
        val updatedBill = bill?.copy(
            name = updatedName,
            color = bill.color.copy(green = 150),
            sharedUsers = listOf(signedInUser)
        )

        put(path).send(updatedBill)

        val request = get(path, 5).send<BillsResponse>()
        val addedItem = request.response.content.parseResponse<BillsResponse>()?.items?.first()

        val id = addedItem?.id
        val className = addedItem!!::class.java.simpleName
        val fields = addedItem.history!!.map { it.field }

        request.response.status() shouldBe OK
        addedItem.name shouldBe updatedName
        addedItem.color.green shouldBe 150
        fields[0] shouldBe "$className $id name"
        fields[1] shouldBe "$className $id sharedUser"
    }

    @Test
    @Order(7)
    fun `verify updating a non existent item`() {
        put(path).sendStatus(newItem(5, 99)) shouldBe HttpStatusCode.BadRequest
    }

    @Test
    @Order(8)
    fun `verify updating without an id`() {
        put(path).sendStatus(newItem(6)) shouldBe BadRequest
    }

    @Test
    @Order(9)
    fun `verify deleting and item that has been added`() {
        post(path).sendStatus(newItem(7)) shouldBe Created
        delete(path, 6).sendStatus<Unit>() shouldBe OK
    }

    @Test
    @Order(10)
    fun `verify deleting item that doesn't exist`() {
        delete(path, 99).sendStatus<Unit>() shouldBe HttpStatusCode.NotFound
    }

    @Test
    @Order(11)
    fun `verify getting complex added item`() {
        post(Path.BillMan.categories).send(Category(name = "randomCategory"))
        val newCategory = get(Path.BillMan.categories).asObject<CategoriesResponse>().items?.last()!!

        post(path).sendStatus(newItem(8).copy(categories = listOf(newCategory))) shouldBe Created

        val addedId = get(path).asObject<BillsResponse>().items?.last()?.id!!

        post(Path.BillMan.occurrences).sendStatus(
            BillOccurrence(
                owner = signedInUser,
                itemId = addedId.toString(),
                amount = newItem(8).amount,
                amountLeft = newItem(8).amount,
                dueDate = System.currentTimeMillis(),
                every = "1 Month"
            )
        ) shouldBe Created

        val response = get(path, addedId).asObject<BillsResponse>().items

        response?.size shouldBe 1
        response?.first()?.owner shouldBe signedInUser.redacted().parse()
    }

    @Test
    @Order(12)
    fun `verify only getting your bills`() {
        post(path).sendStatus(newItem(8)) shouldBe Created

        val newUserToken = post(Path.User.base + Path.User.signUp, usingToken = null).asClass<User, TokenResponse>(
            User(
                name = "test2",
                email = "test2@email.com",
                username = "test2",
                password = "test"
            )
        )?.token

        val newUser = get(Path.User.base + Path.User.account, usingToken = newUserToken).asObject<User>()
        post(path, usingToken = newUserToken).sendStatus(newItem(8, owner = newUser)) shouldBe Created

        val response = get(path, usingToken = newUserToken).asObject<BillsResponse>().items

        response?.size shouldBe 1
        response?.first()?.owner shouldBe newUser.redacted().parse()
    }
}
