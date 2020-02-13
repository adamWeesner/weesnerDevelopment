import io.ktor.server.testing.withTestApplication
import org.junit.After
import java.io.File

open class BaseTest {
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
