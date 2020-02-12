import com.weesnerdevelopment.Paths
import com.weesnerdevelopment.fromJson
import com.weesnerdevelopment.main
import io.ktor.application.Application
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import org.junit.After
import socialSecurity.SocialSecurity
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals


class SocialSecurityTests {
    private val newItem = """
                {
                	"year": 2017,
                	"percent": 1.45,
                	"limit": 127200
                }
                """

    @Test
    fun `verify getting base url returns ok`() = withTestApplication(Application::main) {
        with(handleRequest(HttpMethod.Get, "/${Paths.socialSecurity.name}")) {
            assertEquals(HttpStatusCode.OK, response.status())
        }
    }

    @Test
    fun `verify getting an added item`() = withTestApplication(Application::main) {
        handleRequest(HttpMethod.Post, "/${Paths.socialSecurity.name}") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(newItem)
        }

        with(handleRequest(HttpMethod.Get, "/${Paths.socialSecurity.name}/1") {
        }) {
            val addedItem = response.content!!.fromJson<SocialSecurity>()!!

            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals(
                SocialSecurity(1, 2017, 1.45, 127200, addedItem.dateCreated, addedItem.dateUpdated),
                addedItem
            )
        }
    }

    @Test
    fun `verify putting a new value in gets added`() = withTestApplication(Application::main) {
        with(handleRequest(HttpMethod.Post, "/${Paths.socialSecurity.name}") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(newItem)
        }) {
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
        handleRequest(HttpMethod.Post, "/${Paths.socialSecurity.name}") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(newItem)
        }

        with(handleRequest(HttpMethod.Put, "/${Paths.socialSecurity.name}") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(
                """
                {
                    "id": 1,
                	"year": 2017,
                	"percent": 1.4,
                	"limit": 128000
                }
                """
            )
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
    fun `verify deleting and item that has been added`() = withTestApplication(Application::main) {
        handleRequest(HttpMethod.Post, "/${Paths.socialSecurity.name}") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(newItem)
        }

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

    @After
    fun cleanUp() {
        withTestApplication {
            environment.stop()
        }
        val db = File("server")
        if (db.isDirectory) {
            val children = db.list()
            children?.indices?.forEach { i ->
                File(db, children[i]).delete()
            }
        }
    }
}