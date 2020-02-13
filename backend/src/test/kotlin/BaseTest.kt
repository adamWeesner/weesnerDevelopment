import com.weesnerdevelopment.Paths
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import org.junit.After
import java.io.File

open class BaseTest {
    fun TestApplicationEngine.addNewItem(path: Paths, item: String) =
        handleRequest(HttpMethod.Post, "/${path.name}") {
            addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(item)
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