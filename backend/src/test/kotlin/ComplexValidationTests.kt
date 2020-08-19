import billCategories.BillCategoriesTable
import categories.CategoriesTable
import com.weesnerdevelopment.utils.Path.BillMan
import com.weesnerdevelopment.utils.Path.Server
import com.weesnerdevelopment.validator.complex.ComplexValidatorItem
import com.weesnerdevelopment.validator.complex.ComplexValidatorResponse
import com.weesnerdevelopment.validator.complex.ComplexValidatorTable
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

        BuiltRequest(engine, Post, BillMan.categories, token).sendStatus(
            Category(
                owner = signedInUser, name = "category"
            )
        )
        category = BuiltRequest(engine, Get, "${BillMan.categories}?id=1", token)
            .asObject<CategoriesResponse>().items?.first()!!
    }

    @Test
    @Order(1)
    fun `verify getting base url with no item in the database`() {
        BuiltRequest(engine, Get, path, token).sendStatus<Unit>() shouldBe NoContent
    }

    @Test
    @Order(2)
    fun `verify getting url with an id and with no items in database`() {
        BuiltRequest(engine, Get, "$path?id=1", token).sendStatus<Unit>() shouldBe NoContent
    }

    @Test
    @Order(3)
    fun `verify adding an item to the database`() {
        BuiltRequest(engine, Post, path, token).sendStatus(item()) shouldBe Created

        val getItem = BuiltRequest(engine, Get, "$path?id=1", token).asObject<ComplexValidatorResponse>()
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
        BuiltRequest(engine, Post, path, token).sendStatus(newItem) shouldBe Created
        BuiltRequest(engine, Post, path, token).sendStatus(newItem) shouldBe Conflict
    }

    @Test
    @Order(5)
    fun `verify adding an item with an id to the database`() {
        val newItem = item()
        BuiltRequest(engine, Post, path, token).sendStatus(newItem.copy(id = 1)) shouldBe Created
    }

    @Test
    @Order(6)
    fun `verify getting base url with items in the database`() {
        val response = BuiltRequest(engine, Get, path, token).asObject<ComplexValidatorResponse>()
        response.items?.size shouldBe 3
    }

    @Test
    @Order(7)
    fun `verify update an item in the database`() {
        val savedId = 5
        val newItem = item()

        BuiltRequest(engine, Post, path, token).sendStatus(newItem) shouldBe Created
        BuiltRequest(engine, Post, BillMan.categories, token).send(Category(owner = signedInUser, name = "categoryTwo"))

        val category = BuiltRequest(engine, Get, "${BillMan.categories}?id=2", token)
            .asObject<CategoriesResponse>().items?.first()
            ?: throw IllegalArgumentException("Somehow category was null")

        BuiltRequest(engine, Put, path, token).sendStatus(
            newItem.copy(
                id = savedId,
                amount = 99.99,
                category = category
            )
        ) shouldBe OK

        val getItem = BuiltRequest(engine, Get, "$path?id=$savedId", token).asObject<ComplexValidatorResponse>()
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

        BuiltRequest(engine, Post, path, token).sendStatus(newItem) shouldBe Created
        BuiltRequest(engine, Put, path, token).sendStatus(newItem.copy(id = savedId)) shouldBe OK

        val getItem = BuiltRequest(engine, Get, "$path?id=$savedId", token).asObject<ComplexValidatorResponse>()
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
        BuiltRequest(engine, Post, path, token).sendStatus(newItem) shouldBe Created
        BuiltRequest(engine, Put, path, token).sendStatus(newItem) shouldBe BadRequest
    }

    @Test
    @Order(10)
    fun `verify deleting item in the database`() {
        BuiltRequest(engine, Post, path, token).sendStatus(item()) shouldBe Created
        BuiltRequest(engine, Delete, "$path?id=7", token).sendStatus<Unit>() shouldBe OK

        val getItem = BuiltRequest(engine, Get, path, token).asObject<ComplexValidatorResponse>()
        getItem.items?.firstOrNull { it.id == 7 } shouldBe null
    }

    @Test
    @Order(11)
    fun `verify deleting item that is not in the database`() {
        BuiltRequest(engine, Delete, "$path?id=99", token).sendStatus<Unit>() shouldBe NotFound
    }

    @Test
    @Order(12)
    fun `verify deleting without giving an id`() {
        BuiltRequest(engine, Delete, path, token).sendStatus<Unit>() shouldBe BadRequest
    }
}
