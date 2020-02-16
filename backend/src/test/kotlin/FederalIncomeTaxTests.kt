import MaritalStatus.Single
import PayPeriod.Weekly
import com.weesnerdevelopment.Paths
import com.weesnerdevelopment.fromJson
import com.weesnerdevelopment.toJson
import federalIncomeTax.FederalIncomeTax
import federalIncomeTax.FederalIncomeTaxResponse
import io.kotlintest.shouldBe
import io.ktor.http.HttpMethod.Companion.Delete
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpMethod.Companion.Put
import io.ktor.http.HttpStatusCode

class FederalIncomeTaxTest : BaseTest({
    fun newItem(year: Int) = FederalIncomeTax(
        year = year,
        percent = 10.0,
        payPeriod = Weekly,
        maritalStatus = Single,
        nonTaxable = 0.0,
        plus = 0.0,
        notOver = 1.0,
        over = 1.0
    )

    "verify getting base url returns ok" {
        with(engine) {
            request(Get, Paths.federalIncomeTax).response.status() shouldBe HttpStatusCode.OK
        }
    }

    "verify getting base url returns all items in table" {
        with(engine) {
            bodyRequest(Post, Paths.federalIncomeTax, newItem(2000).toJson())
            bodyRequest(Post, Paths.federalIncomeTax, newItem(2001).toJson())
            with(request(Get, Paths.federalIncomeTax)) {
                val responseItems = response.content?.fromJson<FederalIncomeTaxResponse>()?.items
                val item1 = responseItems!![responseItems.lastIndex - 1]
                val item2 = responseItems[responseItems.lastIndex]
                response.status() shouldBe HttpStatusCode.OK
                item1 shouldBe FederalIncomeTax(
                    item1.id,
                    2000,
                    Single,
                    Weekly,
                    1.0,
                    1.0,
                    0.0,
                    10.0,
                    0.0,
                    item1.dateCreated,
                    item1.dateUpdated
                )
                item2 shouldBe FederalIncomeTax(
                    item2.id,
                    2001,
                    Single,
                    Weekly,
                    1.0,
                    1.0,
                    0.0,
                    10.0,
                    0.0,
                    item2.dateCreated,
                    item2.dateUpdated
                )
            }
        }
    }

    "verify getting an added item" {
        with(engine) {
            val id = requestToObject<FederalIncomeTax>(Post, Paths.federalIncomeTax, newItem(2002).toJson())?.id
            with(request(Get, Paths.federalIncomeTax, id?.toString())) {
                val addedItem = response.content!!.fromJson<FederalIncomeTax>()!!
                response.status() shouldBe HttpStatusCode.OK
                addedItem shouldBe FederalIncomeTax(
                    id,
                    2002,
                    Single,
                    Weekly,
                    1.0,
                    1.0,
                    0.0,
                    10.0,
                    0.0,
                    addedItem.dateCreated,
                    addedItem.dateUpdated
                )
            }
        }
    }

    "verify getting an item that does not exist" {
        with(engine) {
            request(Get, Paths.federalIncomeTax, "99").response.status() shouldBe HttpStatusCode.NotFound
        }
    }

    "verify adding a new item" {
        with(engine) {
            with(bodyRequest(Post, Paths.federalIncomeTax, newItem(2003).toJson())) {
                val addedItem = response.content!!.fromJson<FederalIncomeTax>()!!
                response.status() shouldBe HttpStatusCode.Created
                addedItem shouldBe FederalIncomeTax(
                    addedItem.id,
                    2003,
                    Single,
                    Weekly,
                    1.0,
                    1.0,
                    0.0,
                    10.0,
                    0.0,
                    addedItem.dateCreated,
                    addedItem.dateUpdated
                )
            }
        }
    }

    "verify adding a duplicate item" {
        with(engine) {
            bodyRequest(Post, Paths.federalIncomeTax, newItem(2008).toJson())

            with(bodyRequest(Post, Paths.federalIncomeTax, newItem(2008).toJson())) {
                response.status() shouldBe HttpStatusCode.Conflict
            }
        }
    }

    "verify updating an added item" {
        with(engine) {
            bodyRequest(Post, Paths.federalIncomeTax, newItem(2004).toJson())
            with(
                bodyRequest(
                    Put,
                    Paths.federalIncomeTax,
                    newItem(2004).copy(id = 1, percent = 1.4, over = 2.5).toJson()
                )
            ) {
                val addedItem = response.content!!.fromJson<FederalIncomeTax>()!!
                response.status() shouldBe HttpStatusCode.OK
                addedItem shouldBe FederalIncomeTax(
                    1,
                    2004,
                    Single,
                    Weekly,
                    2.5,
                    1.0,
                    0.0,
                    1.4,
                    0.0,
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
                Paths.federalIncomeTax,
                newItem(2005).copy(99).toJson()
            ).response.status() shouldBe HttpStatusCode.NotFound
        }
    }

    "verify updating without an id adds a new item" {
        with(engine) {
            bodyRequest(
                Put,
                Paths.federalIncomeTax,
                newItem(2006).toJson()
            ).response.status() shouldBe HttpStatusCode.Created
        }
    }

    "verify deleting and item that has been added" {
        with(engine) {
            bodyRequest(Post, Paths.federalIncomeTax, newItem(2007).toJson())
            request(Delete, Paths.federalIncomeTax, "2007").response.status() shouldBe HttpStatusCode.OK
        }
    }

    "verify deleting item that doesn't exist" {
        with(engine) {
            request(Delete, Paths.federalIncomeTax, "2099").response.status() shouldBe HttpStatusCode.NotFound
        }
    }
})