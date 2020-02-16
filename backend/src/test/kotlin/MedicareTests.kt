import com.weesnerdevelopment.Paths
import com.weesnerdevelopment.fromJson
import com.weesnerdevelopment.toJson
import io.kotlintest.shouldBe
import io.ktor.http.HttpMethod.Companion.Delete
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpMethod.Companion.Put
import io.ktor.http.HttpStatusCode
import medicare.Medicare
import medicare.MedicareLimit
import medicare.MedicareResponse


class MedicareTests : BaseTest({
    fun newItem(year: Int) = Medicare(
        year = year,
        percent = 6.25,
        additionalPercent = 0.9,
        limits = listOf(
            MedicareLimit(
                year = year,
                amount = 200000,
                maritalStatus = MaritalStatus.Single
            )
        )
    )

    "verify getting base url returns ok" {
        with(engine) {
            request(Get, Paths.medicare).response.status() shouldBe HttpStatusCode.OK
        }
    }

    "verify getting base url returns all items in table" {
        with(engine) {
            bodyRequest(Post, Paths.medicare, newItem(2000).toJson())
            bodyRequest(Post, Paths.medicare, newItem(2001).toJson())
            with(request(Get, Paths.medicare)) {
                val responseItems = response.content?.fromJson<MedicareResponse>()?.items
                val item1 = responseItems!![responseItems.lastIndex - 1]
                val item2 = responseItems[responseItems.lastIndex]
                response.status() shouldBe HttpStatusCode.OK
                item1 shouldBe Medicare(item1.id, 2000, 6.25, 0.9, item1.limits, item1.dateCreated, item1.dateUpdated)
                item2 shouldBe Medicare(item2.id, 2001, 6.25, 0.9, item2.limits, item2.dateCreated, item2.dateUpdated)
            }
        }
    }

    "verify getting an added item" {
        with(engine) {
            val id = requestToObject<Medicare>(Post, Paths.medicare, newItem(2002).toJson())?.id
            with(request(Get, Paths.medicare, id?.toString())) {
                val addedItem = response.content!!.fromJson<Medicare>()!!
                response.status() shouldBe HttpStatusCode.OK
                addedItem shouldBe Medicare(
                    id,
                    2002,
                    6.25,
                    0.9,
                    addedItem.limits,
                    addedItem.dateCreated,
                    addedItem.dateUpdated
                )
            }
        }
    }

    "verify getting an item that does not exist" {
        with(engine) {
            request(Get, Paths.medicare, "99").response.status() shouldBe HttpStatusCode.NotFound
        }
    }

    "verify adding a new item" {
        with(engine) {
            with(bodyRequest(Post, Paths.medicare, newItem(2003).toJson())) {
                val addedItem = response.content?.fromJson<Medicare>()!!
                response.status() shouldBe HttpStatusCode.Created
                addedItem shouldBe Medicare(
                    addedItem.id,
                    2003,
                    6.25,
                    0.9,
                    listOf(
                        MedicareLimit(
                            addedItem.limits[0].id,
                            2003,
                            MaritalStatus.Single,
                            200000,
                            addedItem.limits[0].dateCreated,
                            addedItem.limits[0].dateUpdated
                        )
                    ),
                    addedItem.dateCreated,
                    addedItem.dateUpdated
                )
            }
        }
    }

    "verify updating an added item" {
        with(engine) {
            val id = requestToObject<Medicare>(Post, Paths.medicare, newItem(2004).toJson())?.id
            with(bodyRequest(Put, Paths.medicare, newItem(2004).copy(id = id, percent = 6.0).toJson())) {
                val addedItem = response.content!!.fromJson<Medicare>()!!
                response.status() shouldBe HttpStatusCode.OK
                addedItem shouldBe Medicare(
                    id,
                    2004,
                    6.0,
                    addedItem.additionalPercent,
                    addedItem.limits,
                    addedItem.dateCreated,
                    addedItem.dateUpdated
                )
            }
        }
    }

    "verify updating a non existent item" {
        with(engine) {
            bodyRequest(
                Put,
                Paths.medicare,
                newItem(2005).copy(99).toJson()
            ).response.status() shouldBe HttpStatusCode.NotFound
        }
    }

    "verify updating without an id adds a new item" {
        with(engine) {
            bodyRequest(Put, Paths.medicare, newItem(2006).toJson()).response.status() shouldBe HttpStatusCode.Created
        }
    }

    "verify deleting and item that has been added" {
        with(engine) {
            bodyRequest(Post, Paths.medicare, newItem(2007).toJson())
            val addedItemId =
                requestToObject<MedicareResponse>(Get, Paths.medicare)?.items?.find { it.year == 2007 }?.id
            request(Delete, Paths.medicare, addedItemId?.toString()).response.status() shouldBe HttpStatusCode.OK
        }
    }

    "verify deleting item that doesn't exist" {
        with(engine) {
            request(Delete, Paths.medicare, "99").response.status() shouldBe HttpStatusCode.NotFound
        }
    }
})