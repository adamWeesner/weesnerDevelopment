import category.CategoriesResponse
import category.Category
import com.weesnerdevelopment.Paths
import com.weesnerdevelopment.fromJson
import com.weesnerdevelopment.toJson
import io.kotlintest.shouldBe
import io.ktor.http.HttpMethod.Companion.Delete
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpMethod.Companion.Put
import io.ktor.http.HttpStatusCode

class CategoriesTests : BaseTest({
    fun newItem(categoryName: String) = Category(
        name = categoryName,
        owner = 1
    )

    "verify getting base url returns ok" {
        with(engine) {
            request(Get, Paths.category).response.status() shouldBe HttpStatusCode.OK
        }
    }

    "verify getting base url returns all items in table" {
        with(engine) {
            bodyRequest(Post, Paths.category, newItem("1").toJson())
            bodyRequest(Post, Paths.category, newItem("2").toJson())
            with(request(Get, Paths.category)) {
                val responseItems = response.content?.fromJson<CategoriesResponse>()?.items
                val item1 = responseItems!![responseItems.lastIndex - 1]
                val item2 = responseItems[responseItems.lastIndex]

                response.status() shouldBe HttpStatusCode.OK

                item1.apply { this shouldBe Category(id, "1", owner, dateCreated, dateUpdated) }
                item2.apply { this shouldBe Category(id, "2", owner, dateCreated, dateUpdated) }
            }
        }
    }

    "verify getting an added item" {
        with(engine) {
            val id = requestToObject<Category>(Post, Paths.category, newItem("3").toJson())?.id
            with(request(Get, Paths.category, id?.toString())) {
                val addedItem = response.content!!.fromJson<Category>()!!

                response.status() shouldBe HttpStatusCode.OK

                addedItem.apply { this shouldBe Category(id, "3", owner, dateCreated, dateUpdated) }
            }
        }
    }

    "verify getting an item that does not exist" {
        with(engine) {
            request(Get, Paths.category, "99").response.status() shouldBe HttpStatusCode.NotFound
        }
    }

    "verify adding a new item" {
        with(engine) {
            with(bodyRequest(Post, Paths.category, newItem("4").toJson())) {
                val addedItem = response.content!!.fromJson<Category>()!!

                response.status() shouldBe HttpStatusCode.Created

                addedItem.apply { this shouldBe Category(id, "4", owner, dateCreated, dateUpdated) }
            }
        }
    }

    "verify updating an added item" {
        with(engine) {
            bodyRequest(Post, Paths.category, newItem("5").toJson())
            with(bodyRequest(Put, Paths.category, newItem("5").copy(owner = 2).toJson())) {
                val addedItem = response.content!!.fromJson<Category>()!!

                response.status() shouldBe HttpStatusCode.OK

                addedItem.apply { this shouldBe Category(id, "5", 2, dateCreated, dateUpdated) }
            }
        }
    }

    "verify updating a non existent item" {
        with(engine) {
            bodyRequest(
                Put, Paths.category, newItem("6").copy(99).toJson()
            ).response.status() shouldBe HttpStatusCode.NotFound
        }
    }

    "verify updating without an id adds a new item" {
        with(engine) {
            bodyRequest(
                Put, Paths.category, newItem("7").toJson()
            ).response.status() shouldBe HttpStatusCode.Created
        }
    }

    "verify deleting and item that has been added" {
        with(engine) {
            bodyRequest(Post, Paths.category, newItem("8").toJson())
            request(Delete, Paths.category, "1").response.status() shouldBe HttpStatusCode.OK
        }
    }

    "verify deleting item that doesn't exist" {
        with(engine) {
            request(Delete, Paths.category, "99").response.status() shouldBe HttpStatusCode.NotFound
        }
    }
})