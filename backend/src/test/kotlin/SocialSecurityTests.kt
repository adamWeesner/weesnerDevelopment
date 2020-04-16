import com.weesnerdevelopment.Path
import io.kotlintest.shouldBe
import io.ktor.http.HttpMethod.Companion.Delete
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpMethod.Companion.Put
import io.ktor.http.HttpStatusCode
import shared.fromJson
import shared.taxFetcher.SocialSecurity
import shared.toJson
import socialSecurity.SocialSecurityResponse

class SocialSecurityTest : BaseTest({
    fun newItem(year: Int) = SocialSecurity(
        year = year,
        percent = 1.45,
        limit = 127200
    )

    val path = Path.TaxFetcher.socialSecurity

    "verify getting base url returns ok" {
        with(engine) {
            request(Get, path).response.status() shouldBe HttpStatusCode.OK
        }
    }

    "verify getting base url returns all items in table" {
        with(engine) {
            bodyRequest(Post, path, newItem(2000).toJson())
            bodyRequest(Post, path, newItem(2001).toJson())
            with(request(Get, path)) {
                val responseItems = response.content?.fromJson<SocialSecurityResponse>()?.items
                val item1 = responseItems!![responseItems.lastIndex - 1]
                val item2 = responseItems[responseItems.lastIndex]
                response.status() shouldBe HttpStatusCode.OK
                item1 shouldBe SocialSecurity(item1.id, 2000, 1.45, 127200, item1.dateCreated, item1.dateUpdated)
                item2 shouldBe SocialSecurity(item2.id, 2001, 1.45, 127200, item2.dateCreated, item2.dateUpdated)
            }
        }
    }

    "verify getting an added item" {
        with(engine) {
            val id = requestToObject<SocialSecurity>(Post, path, newItem(2002).toJson())?.id
            with(request(Get, path, id?.toString())) {
                val addedItem = response.content!!.fromJson<SocialSecurity>()!!
                response.status() shouldBe HttpStatusCode.OK
                addedItem shouldBe SocialSecurity(id, 2002, 1.45, 127200, addedItem.dateCreated, addedItem.dateUpdated)
            }
        }
    }

    "verify adding a duplicate item" {
        with(engine) {
            bodyRequest(Post, path, newItem(2008).toJson())

            with(bodyRequest(Post, path, newItem(2008).toJson())) {
                response.status() shouldBe HttpStatusCode.Conflict
            }
        }
    }

    "verify getting an item that does not exist" {
        with(engine) {
            request(Get, path, "99").response.status() shouldBe HttpStatusCode.NotFound
        }
    }

    "verify adding a new item" {
        with(engine) {
            with(bodyRequest(Post, path, newItem(2003).toJson())) {
                val addedItem = response.content!!.fromJson<SocialSecurity>()!!
                response.status() shouldBe HttpStatusCode.Created
                addedItem shouldBe SocialSecurity(
                    addedItem.id,
                    2003,
                    1.45,
                    127200,
                    addedItem.dateCreated,
                    addedItem.dateUpdated
                )
            }
        }
    }

    "verify updating an added item" {
        with(engine) {
            bodyRequest(Post, path, newItem(2004).toJson())
            with(
                bodyRequest(
                    Put,
                    path,
                    newItem(2004).copy(id = 1, percent = 1.4, limit = 128000).toJson()
                )
            ) {
                val addedItem = response.content!!.fromJson<SocialSecurity>()!!
                response.status() shouldBe HttpStatusCode.OK
                addedItem shouldBe SocialSecurity(1, 2004, 1.4, 128000, addedItem.dateCreated, addedItem.dateUpdated)
            }
        }
    }

    "verify updating a non existent item" {
        with(engine) {
            bodyRequest(
                Put,
                path,
                newItem(2005).copy(99).toJson()
            ).response.status() shouldBe HttpStatusCode.NotFound
        }
    }

    "verify updating without an id adds a new item" {
        with(engine) {
            bodyRequest(
                Put,
                path,
                newItem(2006).toJson()
            ).response.status() shouldBe HttpStatusCode.Created
        }
    }

    "verify deleting and item that has been added" {
        with(engine) {
            bodyRequest(Post, path, newItem(2007).toJson())
            request(Delete, path, "2007").response.status() shouldBe HttpStatusCode.OK
        }
    }

    "verify deleting item that doesn't exist" {
        with(engine) {
            request(Delete, path, "2099").response.status() shouldBe HttpStatusCode.NotFound
        }
    }
})