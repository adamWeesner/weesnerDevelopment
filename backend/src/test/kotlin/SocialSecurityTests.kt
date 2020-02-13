import com.weesnerdevelopment.Paths
import com.weesnerdevelopment.fromJson
import com.weesnerdevelopment.main
import com.weesnerdevelopment.toJson
import io.kotlintest.shouldBe
import io.ktor.application.Application
import io.ktor.http.HttpMethod.Companion.Delete
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpMethod.Companion.Put
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.withTestApplication
import socialSecurity.SocialSecurity
import socialSecurity.SocialSecurityResponse
import kotlin.test.Test

class SocialSecurityTests : BaseTest() {
    private fun newItem(year: Int) = SocialSecurity(
        year = year,
        percent = 1.45,
        limit = 127200
    )

    @Test
    fun `verify getting base url returns ok`() = withTestApplication(Application::main) {
        request(Get, Paths.socialSecurity).response.status() shouldBe HttpStatusCode.OK
    }

    @Test
    fun `verify getting base url returns all items in table`() = withTestApplication(Application::main) {
        bodyRequest(Post, Paths.socialSecurity, newItem(2000).toJson())
        bodyRequest(Post, Paths.socialSecurity, newItem(2001).toJson())

        with(request(Get, Paths.socialSecurity)) {
            val responseItems = response.content?.fromJson<SocialSecurityResponse>()?.socialSecurity
            val item1 = responseItems!![responseItems.lastIndex - 1]
            val item2 = responseItems[responseItems.lastIndex]

            response.status() shouldBe HttpStatusCode.OK

            item1 shouldBe SocialSecurity(item1.id, 2000, 1.45, 127200, item1.dateCreated, item1.dateUpdated)
            item2 shouldBe SocialSecurity(item2.id, 2001, 1.45, 127200, item2.dateCreated, item2.dateUpdated)
        }
    }

    @Test
    fun `verify getting an added item`() = withTestApplication(Application::main) {
        val id = requestToObject<SocialSecurity>(Post, Paths.socialSecurity, newItem(2002).toJson())?.id

        with(request(Get, Paths.socialSecurity, id?.toString())) {
            val addedItem = response.content!!.fromJson<SocialSecurity>()!!

            response.status() shouldBe HttpStatusCode.OK
            addedItem shouldBe SocialSecurity(id, 2002, 1.45, 127200, addedItem.dateCreated, addedItem.dateUpdated)
        }
    }

    @Test
    fun `verify getting an item that does not exist`() = withTestApplication(Application::main) {
        request(Get, Paths.socialSecurity, "99").response.status() shouldBe HttpStatusCode.NotFound
    }

    @Test
    fun `verify adding a new item`() = withTestApplication(Application::main) {
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

    @Test
    fun `verify updating an added item`() = withTestApplication(Application::main) {
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

    @Test
    fun `verify updating a non existent item`() = withTestApplication(Application::main) {
        bodyRequest(
            Put,
            Paths.socialSecurity,
            newItem(2005).copy(99).toJson()
        ).response.status() shouldBe HttpStatusCode.NotFound
    }

    @Test
    fun `verify updating without an id adds a new item`() = withTestApplication(Application::main) {
        bodyRequest(Put, Paths.socialSecurity, newItem(2006).toJson()).response.status() shouldBe HttpStatusCode.Created
    }

    @Test
    fun `verify deleting and item that has been added`() = withTestApplication(Application::main) {
        bodyRequest(Post, Paths.socialSecurity, newItem(2007).toJson())
        request(Delete, Paths.socialSecurity, "1").response.status() shouldBe HttpStatusCode.OK
    }

    @Test
    fun `verify deleting item that doesn't exist`() = withTestApplication(Application::main) {
        request(Delete, Paths.socialSecurity, "99").response.status() shouldBe HttpStatusCode.NotFound
    }
}