import Path.Server
import com.weesnerdevelopment.test.utils.BaseTest
import com.weesnerdevelopment.test.utils.shouldBe
import com.weesnerdevelopment.validator.ValidatorItem
import com.weesnerdevelopment.validator.ValidatorResponse
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Conflict
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.NoContent
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.OK
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test

class ValidationTests : BaseTest() {
    private var counter = 1
    private val item: ValidatorItem
        get() = ValidatorItem(
            name = "item$counter",
            amount = 11.34 + counter
        ).also {
            counter++
        }

    val path = Server.validation

    @Test
    @Order(1)
    fun `verify getting base url with no item in the database`() {
        get(path).sendStatus<Unit>() shouldBe NoContent
    }

    @Test
    @Order(2)
    fun `verify getting url with an id and with no items in database`() {
        get(path, 0).sendStatus<Unit>() shouldBe NoContent
    }

    @Test
    @Order(3)
    fun `verify adding an item to the database`() {
        post(path).sendStatus(item) shouldBe Created

        val getItem = get(path, 1).asObject<ValidatorResponse>()
        val firstItem = getItem.items?.first()!!

        getItem.items?.size shouldBe 1
        firstItem shouldBe ValidatorItem(
            1,
            "item1",
            12.34,
            firstItem.dateCreated,
            firstItem.dateUpdated
        )
    }

    @Test
    @Order(4)
    fun `verify adding an item that already exists to the database`() {
        val newItem = item
        post(path).sendStatus(newItem) shouldBe Created
        post(path).sendStatus(newItem) shouldBe Conflict
    }

    @Test
    @Order(6)
    fun `verify getting base url with items in the database`() {
        val response = get(path).asObject<ValidatorResponse>()
        response.items?.size shouldBe 2
    }

    @Test
    @Order(7)
    fun `verify update an item in the database`() {
        val savedId = 4
        val newItem = item
        post(path).sendStatus(newItem) shouldBe Created
        put(path).sendStatus(newItem.copy(id = savedId, amount = 99.99)) shouldBe OK

        val getItem = get(path, savedId).asObject<ValidatorResponse>()
        val firstItem = getItem.items?.first()

        getItem.items?.size shouldBe 1
        firstItem shouldBe ValidatorItem(
            savedId,
            "item${savedId - 1}",
            99.99,
            firstItem!!.dateCreated,
            firstItem.dateUpdated
        )
    }

    @Test
    @Order(8)
    fun `verify update an item in the database with no data changed`() {
        val savedId = 5
        val newItem = item
        post(path).sendStatus(newItem) shouldBe Created
        put(path).sendStatus(newItem.copy(id = savedId)) shouldBe OK

        val getItem = get(path, savedId).asObject<ValidatorResponse>()
        val firstItem = getItem.items?.first()!!

        getItem.items?.size shouldBe 1
        firstItem shouldBe ValidatorItem(
            firstItem.id,
            "item4",
            firstItem.amount,
            firstItem.dateCreated,
            firstItem.dateUpdated
        )
    }

    @Test
    @Order(9)
    fun `verify update an item in the database with no id`() {
        val newItem = item
        post(path).sendStatus(newItem) shouldBe Created
        put(path).sendStatus(newItem) shouldBe BadRequest
    }

    @Test
    @Order(10)
    fun `verify deleting item in the database`() {
        post(path).sendStatus(item) shouldBe Created
        delete(path, 7).sendStatus<Unit>() shouldBe OK
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
