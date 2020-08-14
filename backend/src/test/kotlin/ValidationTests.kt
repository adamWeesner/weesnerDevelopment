import com.weesnerdevelopment.utils.Path.Server
import com.weesnerdevelopment.validator.ValidatorItem
import com.weesnerdevelopment.validator.ValidatorResponse
import io.kotlintest.shouldBe
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

private var counter = 1
private val item: ValidatorItem
    get() = ValidatorItem(
        name = "item$counter",
        amount = 11.34 + counter
    ).also {
        counter++
    }

class ValidationTests : BaseTest({ token ->
    val path = Server.validation

    "verify getting base url with no item in the database" {
        BuiltRequest(engine, Get, path, token).sendStatus<Unit>() shouldBe NoContent
    }

    "verify getting url with an id and with no items in database" {
        BuiltRequest(engine, Get, "$path?id=0", token).sendStatus<Unit>() shouldBe NoContent
    }

    "verify adding an item to the database" {
        BuiltRequest(engine, Post, path, token).sendStatus(item) shouldBe Created

        val getItem = BuiltRequest(engine, Get, "$path?id=1", token).asObject<ValidatorResponse>()
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

    "verify adding an item that already exists to the database" {
        val newItem = item
        BuiltRequest(engine, Post, path, token).sendStatus(newItem) shouldBe Created
        BuiltRequest(engine, Post, path, token).sendStatus(newItem) shouldBe Conflict
    }

    "verify adding an item with an id to the database" {
        val newItem = item
        BuiltRequest(engine, Post, path, token).sendStatus(newItem.copy(id = 1)) shouldBe Created
    }

    "verify getting base url with items in the database" {
        val response = BuiltRequest(engine, Get, path, token).asObject<ValidatorResponse>()
        response.items?.size shouldBe 3
    }

    "verify update an item in the database" {
        val savedId = 5
        val newItem = item
        BuiltRequest(engine, Post, path, token).sendStatus(newItem) shouldBe Created
        BuiltRequest(engine, Put, path, token).sendStatus(newItem.copy(id = savedId, amount = 99.99)) shouldBe OK

        val getItem = BuiltRequest(engine, Get, "$path?id=$savedId", token).asObject<ValidatorResponse>()
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

    "verify update an item in the database with no data changed" {
        val savedId = 6
        val newItem = item
        BuiltRequest(engine, Post, path, token).sendStatus(newItem) shouldBe Created
        BuiltRequest(engine, Put, path, token).sendStatus(newItem.copy(id = savedId)) shouldBe OK

        val getItem = BuiltRequest(engine, Get, "$path?id=$savedId", token).asObject<ValidatorResponse>()
        val firstItem = getItem.items?.first()!!

        getItem.items?.size shouldBe 1
        firstItem shouldBe ValidatorItem(
            firstItem.id,
            "item5",
            firstItem.amount,
            firstItem.dateCreated,
            firstItem.dateUpdated
        )
    }

    "verify update an item in the database with no id" {
        val newItem = item
        BuiltRequest(engine, Post, path, token).sendStatus(newItem) shouldBe Created
        BuiltRequest(engine, Put, path, token).sendStatus(newItem) shouldBe BadRequest
    }

    "verify deleting item in the database" {
        BuiltRequest(engine, Post, path, token).sendStatus(item) shouldBe Created
        BuiltRequest(engine, Delete, "$path?id=7", token).sendStatus<Unit>() shouldBe OK
    }

    "verify deleting item that is not in the database" {
        BuiltRequest(engine, Delete, "$path?id=99", token).sendStatus<Unit>() shouldBe NotFound
    }

    "verify deleting without giving an id" {
        BuiltRequest(engine, Delete, path, token).sendStatus<Unit>() shouldBe BadRequest
    }
})