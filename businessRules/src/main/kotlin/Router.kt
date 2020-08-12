import io.ktor.routing.Route
import shared.base.GenericItem
import kotlin.reflect.KType

interface Router<I : GenericItem, S : Service<I>> {
    val service: Service<I>
    val basePath: String
    val kType: KType

    fun Route.setupRoutes()
    fun Route.addRequest()
    fun Route.getRequest()
    fun Route.updateRequest()
    fun Route.deleteRequest()
}
