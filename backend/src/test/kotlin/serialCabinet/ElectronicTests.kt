package serialCabinet

import Path
import com.weesnerdevelopment.shared.currentTimeMillis
import com.weesnerdevelopment.shared.serialCabinet.Category
import com.weesnerdevelopment.shared.serialCabinet.Electronic
import com.weesnerdevelopment.shared.serialCabinet.Manufacturer
import com.weesnerdevelopment.shared.serialCabinet.responses.CategoriesResponse
import com.weesnerdevelopment.shared.serialCabinet.responses.ElectronicsResponse
import com.weesnerdevelopment.shared.serialCabinet.responses.ManufacturersResponse
import com.weesnerdevelopment.test.utils.BaseTest
import com.weesnerdevelopment.test.utils.parseResponse
import com.weesnerdevelopment.test.utils.shouldBe
import com.weesnerdevelopment.test.utils.shouldNotBe
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Conflict
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.NoContent
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.util.*
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.SchemaUtils.drop
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import serialCabinet.category.SerialCategoriesTable
import serialCabinet.electronic.ElectronicsTable
import serialCabinet.itemCategories.SerialItemCategoriesTable
import serialCabinet.manufacturer.ManufacturersTable

@KtorExperimentalAPI
class ElectronicTests : BaseTest("application-test.conf") {
    lateinit var startManufacturer: Manufacturer
    lateinit var startCategory: Category

    fun newItem(addition: Int, id: Int? = null) = Electronic(
        id,
        addition.toString(),
        "$addition description",
        null,
        "model-123",
        "1233-1233-1233-1233",
        listOf(startCategory),
        "123123123123",
        null,
        currentTimeMillis(),
        startManufacturer,
        currentTimeMillis(),
        signedInUser
    )

    val path = Path.SerialCabinet.electronics

    @BeforeAll
    fun start() {
        transaction {
            drop(ElectronicsTable, ManufacturersTable, SerialCategoriesTable, SerialItemCategoriesTable)
            create(ElectronicsTable, SerialCategoriesTable, ManufacturersTable, SerialItemCategoriesTable)
        }

        createUser()

        post(Path.SerialCabinet.categories).send(Category(name = "randomCategory", description = "description"))
        startCategory = get(Path.SerialCabinet.categories).asObject<CategoriesResponse>().items?.last()!!

        post(Path.SerialCabinet.manufacturers).send(Manufacturer(name = "randomManufacturer"))
        startManufacturer = get(Path.SerialCabinet.manufacturers).asObject<ManufacturersResponse>().items?.last()!!
    }

    @AfterAll
    fun destroy() {
        transaction {
            drop(ElectronicsTable, ManufacturersTable, SerialCategoriesTable, SerialItemCategoriesTable)
            create(ElectronicsTable, SerialCategoriesTable, ManufacturersTable, SerialItemCategoriesTable)
        }
    }

    @Test
    @Order(1)
    fun `verify getting base url`() {
        get(path).sendStatus<Unit>() shouldBe NoContent
    }

    @Test
    @Order(2)
    fun `verify getting base url returns all items in table`() {
        post(path).sendStatus(newItem(0)) shouldBe Created
        post(path).sendStatus(newItem(1)) shouldBe Created

        val request = get(path).send<Unit>()
        val responseItems = request.response.content.parseResponse<ElectronicsResponse>()?.items

        val item1 = responseItems!![responseItems.lastIndex - 1]
        val item2 = responseItems[responseItems.lastIndex]
        request.response.status() shouldBe OK

        item1.name shouldBe "0"
        item2.name shouldBe "1"
    }

    @Test
    @Order(3)
    fun `verify getting an added item`() {
        post(path).sendStatus(newItem(2)) shouldBe Created

        val request = get(path).send<Electronic>()
        val addedItems = request.response.content.parseResponse<ElectronicsResponse>()?.items?.last()

        request.response.status() shouldBe OK

        addedItems?.name shouldBe "2"

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
    }

    @Test
    @Order(6)
    fun `verify adding a duplicate item`() {
        post(path).send(newItem(8))
        post(path).sendStatus((newItem(8, 5))) shouldBe Conflict
    }

    @Test
    @Order(7)
    fun `verify updating an added item`() {
        val updatedName = "electronic4"
        post(path).sendStatus(newItem(4)) shouldBe Created

        val electronic = get(path).asObject<ElectronicsResponse>().items?.last()

        put(path).sendStatus(electronic?.copy(name = updatedName)) shouldBe OK

        val updatedElectronic = get(path, electronic?.id).asObject<ElectronicsResponse>().items?.first()

        updatedElectronic shouldNotBe null
        updatedElectronic?.name shouldBe updatedName

    }

    @Test
    @Order(8)
    fun `verify updating a non existent item`() {
        put(path).sendStatus(newItem(5, 99)) shouldBe BadRequest
    }

    @Test
    @Order(9)
    fun `verify updating without an id`() {
        put(path).sendStatus(newItem(6)) shouldBe BadRequest
    }

    @Test
    @Order(10)
    fun `verify deleting and item that has been added`() {
        post(path).sendStatus(newItem(7)) shouldBe Created

        val addedItem = get(path).asObject<ElectronicsResponse>().items?.last()

        delete(path, addedItem?.id).sendStatus<Unit>() shouldBe OK
    }

    @Test
    @Order(11)
    fun `verify deleting item that doesn't exist`() {
        delete(path, 99).sendStatus<Unit>() shouldBe NotFound
    }
}
