import com.weesnerdevelopment.Paths
import com.weesnerdevelopment.fromJson
import com.weesnerdevelopment.main
import com.weesnerdevelopment.toJson
import io.kotlintest.shouldBe
import io.ktor.application.Application
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import socialSecurity.SocialSecurity
import socialSecurity.SocialSecurityResponse
import kotlin.test.Test
import kotlin.test.assertEquals


class SocialSecurityTests : BaseTest() {
    private val newItem = SocialSecurity(
        year = 2017,
        percent = 1.45,
        limit = 127200
    )

    @Test
    fun `verify getting base url returns ok`() = withTestApplication(Application::main) {
        with(handleRequest(HttpMethod.Get, "/${Paths.socialSecurity.name}")) {
            assertEquals(HttpStatusCode.OK, response.status())
        }
    }

    @Test
    fun `verify getting base url returns all items in table`() = withTestApplication(Application::main) {
        addNewItem(Paths.socialSecurity, newItem.toJson())
        addNewItem(Paths.socialSecurity, newItem.toJson())

        with(handleRequest(HttpMethod.Get, "/${Paths.socialSecurity.name}")) {
            val responseItems = response.content?.fromJson<SocialSecurityResponse>()?.socialSecurity
            val item1 = responseItems!![responseItems.lastIndex - 1]
            val item2 = responseItems[responseItems.lastIndex]
            assertEquals(HttpStatusCode.OK, response.status())
            item1 shouldBe SocialSecurity(item1.id, 2017, 1.45, 127200, item1.dateCreated, item1.dateUpdated)
            item2 shouldBe SocialSecurity(item2.id, 2017, 1.45, 127200, item2.dateCreated, item2.dateUpdated)
        }
    }

    @Test
    fun `verify getting an added item`() = withTestApplication(Application::main) {
        addNewItem(Paths.socialSecurity, newItem.toJson())

        with(handleRequest(HttpMethod.Get, "/${Paths.socialSecurity.name}/1")) {
            val addedItem = response.content!!.fromJson<SocialSecurity>()!!

            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals(
                SocialSecurity(1, 2017, 1.45, 127200, addedItem.dateCreated, addedItem.dateUpdated),
                addedItem
            )
        }
    }

    @Test
    fun `verify getting an item that does not exist`() = withTestApplication(Application::main) {
        with(handleRequest(HttpMethod.Get, "/${Paths.socialSecurity.name}/99")) {
            assertEquals(HttpStatusCode.NotFound, response.status())
        }
    }

    @Test
    fun `verify adding a new item`() = withTestApplication(Application::main) {
        with(addNewItem(Paths.socialSecurity, newItem.toJson())) {
            val addedItem = response.content!!.fromJson<SocialSecurity>()!!

            assertEquals(HttpStatusCode.Created, response.status())
            assertEquals(
                SocialSecurity(addedItem.id, 2017, 1.45, 127200, addedItem.dateCreated, addedItem.dateUpdated),
                addedItem
            )
        }
    }

    @Test
    fun `verify updating an added item`() = withTestApplication(Application::main) {
        addNewItem(Paths.socialSecurity, newItem.toJson())

        with(handleRequest(HttpMethod.Put, "/${Paths.socialSecurity.name}") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(newItem.copy(id = 1, percent = 1.4, limit = 128000).toJson())
        }) {
            val addedItem = response.content!!.fromJson<SocialSecurity>()!!

            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals(
                SocialSecurity(1, 2017, 1.4, 128000, addedItem.dateCreated, addedItem.dateUpdated),
                addedItem
            )
        }
    }

    @Test
    fun `verify updating a non existent item`() = withTestApplication(Application::main) {
        with(handleRequest(HttpMethod.Put, "/${Paths.socialSecurity.name}") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(newItem.copy(99).toJson())
        }) {
            assertEquals(HttpStatusCode.NotFound, response.status())
        }
    }

    @Test
    fun `verify updating without an id adds a new item`() = withTestApplication(Application::main) {
        with(handleRequest(HttpMethod.Put, "/${Paths.socialSecurity.name}") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(newItem.toJson())
        }) {
            assertEquals(HttpStatusCode.Created, response.status())
        }
    }

    @Test
    fun `verify deleting and item that has been added`() = withTestApplication(Application::main) {
        addNewItem(Paths.socialSecurity, newItem.toJson())

        with(handleRequest(HttpMethod.Delete, "/${Paths.socialSecurity.name}/1") {
        }) {
            assertEquals(HttpStatusCode.OK, response.status())
        }
    }

    @Test
    fun `verify deleting item that doesnt exist`() = withTestApplication(Application::main) {
        with(handleRequest(HttpMethod.Delete, "/${Paths.socialSecurity.name}/99") {
        }) {
            assertEquals(HttpStatusCode.NotFound, response.status())
        }
    }
}