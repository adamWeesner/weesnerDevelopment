import Path.BillMan
import Path.Server
import billCategories.BillCategoriesTable
import categories.CategoriesTable
import com.weesnerdevelopment.validator.complex.ComplexValidatorItem
import com.weesnerdevelopment.validator.complex.ComplexValidatorResponse
import com.weesnerdevelopment.validator.complex.ComplexValidatorTable
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Conflict
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.NoContent
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import shared.base.History
import shared.billMan.Category
import shared.billMan.responses.CategoriesResponse

class ComplexValidationTests : BaseTest() {
    val path = Server.complexValidation
    var counter = 1
    lateinit var category: Category

    fun item(): ComplexValidatorItem = ComplexValidatorItem(
        owner = signedInUser,
        name = "item$counter",
        amount = 11.34 + counter,
        category = category
    ).also {
        counter++
    }

    @BeforeAll
    fun start() {
        transaction {
            SchemaUtils.drop(CategoriesTable, BillCategoriesTable, ComplexValidatorTable)
            SchemaUtils.create(CategoriesTable, BillCategoriesTable, ComplexValidatorTable)
        }

        post(BillMan.categories).sendStatus(Category(owner = signedInUser, name = "category"))
        category = get(BillMan.categories, 1).asObject<CategoriesResponse>().items?.last()!!
    }

    @Test
    @Order(1)
    fun `verify getting base url with no item in the database`() {
        get(path).sendStatus<Unit>() shouldBe NoContent
    }

    @Test
    @Order(2)
    fun `verify getting url with an id and with no items in database`() {
        get(path, 1).sendStatus<Unit>() shouldBe NoContent
    }

    @Test
    @Order(3)
    fun `verify adding an item to the database`() {
        post(path).sendStatus(item()) shouldBe Created

        val getItem = get(path, 1).asObject<ComplexValidatorResponse>()
        val firstItem = getItem.items?.first()!!

        getItem.items?.size shouldBe 1
        firstItem shouldBe ComplexValidatorItem(
            1,
            signedInUser,
            "item1",
            12.34,
            category,
            firstItem.history,
            firstItem.dateCreated,
            firstItem.dateUpdated
        )
    }

    @Test
    @Order(4)
    fun `verify adding an item that already exists to the database`() {
        val newItem = item()
        post(path).sendStatus(newItem) shouldBe Created
        post(path).sendStatus(newItem) shouldBe Conflict
    }

    @Test
    @Order(5)
    fun `verify adding an item with an id to the database`() {
        val newItem = item()
        post(path).sendStatus(newItem.copy(id = 1)) shouldBe Created
    }

    @Test
    @Order(6)
    fun `verify getting base url with items in the database`() {
        val response = get(path).asObject<ComplexValidatorResponse>()
        response.items?.size shouldBe 3
    }

    @Test
    @Order(7)
    fun `verify update an item in the database`() {
        val savedId = 5
        val newItem = item()

        post(path).sendStatus(newItem) shouldBe Created
        post(BillMan.categories).send(Category(owner = signedInUser, name = "categoryTwo"))

        val category = get(BillMan.categories, 2).asObject<CategoriesResponse>().items?.first()
            ?: throw IllegalArgumentException("Somehow category was null")

        put(path).sendStatus(newItem.copy(id = savedId, amount = 99.99, category = category)) shouldBe OK

        val getItem = get(path, savedId).asObject<ComplexValidatorResponse>()
        val firstItem = getItem.items?.first()!!
        val history = firstItem.history!!

        getItem.items?.size shouldBe 1
        firstItem.history!!.size shouldBe 2
        history.first() shouldBe History(
            history.first().id,
            "${ComplexValidatorItem::class.simpleName} ${firstItem.id} amount",
            "15.34",
            "99.99",
            signedInUser,
            history.first().dateCreated,
            history.first().dateUpdated
        )
        history[1] shouldBe History(
            history[1].id,
            "${ComplexValidatorItem::class.simpleName} ${firstItem.id} ${Category::class.simpleName}",
            "1",
            "2",
            signedInUser,
            history[1].dateCreated,
            history[1].dateUpdated
        )
        firstItem shouldBe ComplexValidatorItem(
            savedId,
            signedInUser,
            "item${savedId - 1}",
            99.99,
            category,
            firstItem.history,
            firstItem.dateCreated,
            firstItem.dateUpdated
        )
    }

    @Test
    @Order(8)
    fun `verify update an item in the database with no data changed`() {
        val savedId = 6
        val newItem = item()

        post(path).sendStatus(newItem) shouldBe Created
        put(path).sendStatus(newItem.copy(id = savedId)) shouldBe OK

        val getItem = get(path, savedId).asObject<ComplexValidatorResponse>()
        val firstItem = getItem.items?.first()!!

        getItem.items?.size shouldBe 1
        firstItem shouldBe ComplexValidatorItem(
            firstItem.id,
            signedInUser,
            "item5",
            firstItem.amount,
            category,
            firstItem.history,
            firstItem.dateCreated,
            firstItem.dateUpdated
        )
    }

    @Test
    @Order(9)
    fun `verify update an item in the database with no id`() {
        val newItem = item()
        post(path).sendStatus(newItem) shouldBe Created
        put(path).sendStatus(newItem) shouldBe BadRequest
    }

    @Test
    @Order(10)
    fun `verify deleting item in the database`() {
        post(path).sendStatus(item()) shouldBe Created
        delete(path, 7).sendStatus<Unit>() shouldBe OK

        val getItem = get(path).asObject<ComplexValidatorResponse>()
        getItem.items?.firstOrNull { it.id == 7 } shouldBe null
    }

    @Test
    @Order(11)
    fun `verify deleting item that is not in the database`() {
        delete(path, 99).sendStatus<Unit>() shouldBe NotFound
    }

    @Test
    @Order(12)
    fun `verify deleting without giving an id`() {
        delete(path).sendStatus<Unit>() shouldBe BadRequest
    }
}
