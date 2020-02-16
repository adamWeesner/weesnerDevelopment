import com.weesnerdevelopment.Paths
import com.weesnerdevelopment.fromJson
import com.weesnerdevelopment.toJson
import io.kotlintest.shouldBe
import io.ktor.http.HttpMethod.Companion.Delete
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpMethod.Companion.Put
import io.ktor.http.HttpStatusCode
import socialSecurity.SocialSecurity
import socialSecurity.SocialSecurityResponse

class SocialSecurityTest : BaseTest({
    fun newItem(year: Int) = SocialSecurity(
        year = year,
        percent = 1.45,
        limit = 127200
    )

    "verify getting base url returns ok" {
        with(engine) {
            request(Get, Paths.socialSecurity).response.status() shouldBe HttpStatusCode.OK
        }
    }

    "verify getting base url returns all items in table" {
        with(engine) {
            bodyRequest(Post, Paths.socialSecurity, newItem(2000).toJson())
            bodyRequest(Post, Paths.socialSecurity, newItem(2001).toJson())
            with(request(Get, Paths.socialSecurity)) {
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
            val id = requestToObject<SocialSecurity>(Post, Paths.socialSecurity, newItem(2002).toJson())?.id
            with(request(Get, Paths.socialSecurity, id?.toString())) {
                val addedItem = response.content!!.fromJson<SocialSecurity>()!!
                response.status() shouldBe HttpStatusCode.OK
                addedItem shouldBe SocialSecurity(id, 2002, 1.45, 127200, addedItem.dateCreated, addedItem.dateUpdated)
            }
        }
    }

    "verify getting an item that does not exist" {
        with(engine) {
            request(Get, Paths.socialSecurity, "99").response.status() shouldBe HttpStatusCode.NotFound
        }
    }

    "verify adding a new item" {
        with(engine) {
            with(bodyRequest(Post, Paths.socialSecurity, newItem(2003).toJson())) {
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
            bodyRequest(Post, Paths.socialSecurity, newItem(2004).toJson())
            with(
                bodyRequest(
                    Put,
                    Paths.socialSecurity,
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
                Paths.socialSecurity,
                newItem(2005).copy(99).toJson()
            ).response.status() shouldBe HttpStatusCode.NotFound
        }
    }

    "verify updating without an id adds a new item" {
        with(engine) {
            bodyRequest(
                Put,
                Paths.socialSecurity,
                newItem(2006).toJson()
            ).response.status() shouldBe HttpStatusCode.Created
        }
    }

    "verify deleting and item that has been added" {
        with(engine) {
            bodyRequest(Post, Paths.socialSecurity, newItem(2007).toJson())
            request(Delete, Paths.socialSecurity, "1").response.status() shouldBe HttpStatusCode.OK
        }
    }

    "verify deleting item that doesn't exist" {
        with(engine) {
            request(Delete, Paths.socialSecurity, "99").response.status() shouldBe HttpStatusCode.NotFound
        }
    }
})