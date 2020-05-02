import com.weesnerdevelopment.utils.Path
import io.kotlintest.shouldBe
import io.ktor.http.HttpMethod.Companion.Delete
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpMethod.Companion.Put
import io.ktor.http.HttpStatusCode
import shared.fromJson
import shared.taxFetcher.PayPeriod.Biweekly
import shared.taxFetcher.PayPeriod.Weekly
import shared.taxFetcher.TaxWithholding
import shared.taxFetcher.TaxWithholdingTypes.General
import shared.toJson
import taxWithholding.TaxWithholdingResponse

class TaxWithholdingTests : BaseTest({ token ->
    fun newItem(year: Int) = TaxWithholding(
        year = year,
        amount = 1.23,
        payPeriod = Weekly,
        type = General
    )

    val path = Path.TaxFetcher.taxWithholding

    "verify getting base url returns ok" {
        with(engine) {
            request(Get, path, authToken = token).response.status() shouldBe HttpStatusCode.OK
        }
    }

    "verify getting base url returns all items in table" {
        with(engine) {
            bodyRequest(Post, path, newItem(2000).toJson(), token)
            bodyRequest(Post, path, newItem(2001).toJson(), token)

            with(request(Get, path, authToken = token)) {
                val responseItems = response.content?.fromJson<TaxWithholdingResponse>()?.items
                val item1 = responseItems!![responseItems.lastIndex - 1]
                val item2 = responseItems[responseItems.lastIndex]

                response.status() shouldBe HttpStatusCode.OK

                item1 shouldBe TaxWithholding(
                    item1.id,
                    2000,
                    General,
                    Weekly,
                    1.23,
                    null,
                    item1.dateCreated,
                    item1.dateUpdated
                )
                item2 shouldBe TaxWithholding(
                    item2.id,
                    2001,
                    General,
                    Weekly,
                    1.23,
                    null,
                    item2.dateCreated,
                    item2.dateUpdated
                )
            }
        }
    }

    "verify getting an added item" {
        with(engine) {
            val item = requestToObject<TaxWithholding>(Post, path, newItem(2002).toJson(), token)

            with(request(Get, path, item?.year?.toString(), authToken = token)) {
                val addedItem = response.content!!.fromJson<TaxWithholding>()!!

                response.status() shouldBe HttpStatusCode.OK
                addedItem shouldBe TaxWithholding(
                    item?.id,
                    2002,
                    General,
                    Weekly,
                    1.23,
                    null,
                    addedItem.dateCreated,
                    addedItem.dateUpdated
                )
            }
        }
    }

    "verify adding a duplicate item" {
        with(engine) {
            bodyRequest(Post, path, newItem(2008).toJson(), token)

            with(bodyRequest(Post, path, newItem(2008).toJson(), token)) {
                response.status() shouldBe HttpStatusCode.Conflict
            }
        }
    }

    "verify getting an item that does not exist" {
        with(engine) {
            request(Get, path, "99", authToken = token).response.status() shouldBe HttpStatusCode.NotFound
        }
    }

    "verify adding a new item" {
        with(engine) {
            with(bodyRequest(Post, path, newItem(2003).toJson(), token)) {
                val addedItem = response.content!!.fromJson<TaxWithholding>()!!

                response.status() shouldBe HttpStatusCode.Created
                addedItem shouldBe TaxWithholding(
                    addedItem.id,
                    2003,
                    General,
                    Weekly,
                    1.23,
                    null,
                    addedItem.dateCreated,
                    addedItem.dateUpdated
                )
            }
        }
    }

    "verify updating an added item" {
        with(engine) {
            bodyRequest(Post, path, newItem(2004).toJson(), token)

            with(
                bodyRequest(
                    Put,
                    path,
                    newItem(2004).copy(id = 1, amount = 1.4, payPeriod = Biweekly).toJson(),
                    token
                )
            ) {
                val addedItem = response.content!!.fromJson<TaxWithholding>()!!

                response.status() shouldBe HttpStatusCode.OK
                addedItem shouldBe TaxWithholding(
                    1,
                    2004,
                    General,
                    Biweekly,
                    1.4,
                    null,
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
                path,
                newItem(2005).copy(99).toJson(),
                token
            ).response.status() shouldBe HttpStatusCode.BadRequest
        }
    }

    "verify updating without an id adds a new item" {
        with(engine) {
            bodyRequest(
                Put,
                path,
                newItem(2006).toJson(),
                token
            ).response.status() shouldBe HttpStatusCode.Created
        }
    }

    "verify deleting and item that has been added" {
        with(engine) {
            bodyRequest(Post, path, newItem(2007).toJson(), token)
            request(Delete, path, "2007", authToken = token).response.status() shouldBe HttpStatusCode.OK
        }
    }

    "verify deleting item that doesn't exist" {
        with(engine) {
            request(Delete, path, "2099", authToken = token).response.status() shouldBe HttpStatusCode.NotFound
        }
    }
})
