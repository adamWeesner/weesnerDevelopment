package generics

import io.ktor.routing.Route
import io.ktor.routing.route
import io.ktor.websocket.webSocket
import kotlin.reflect.full.createType

/**
 * Route at /[basePath] using the given [router].
 */
inline fun <reified O : GenericItem, T : IdTable> Route.route(
    basePath: String,
    router: GenericRouter<O, T>
) {
    route("/$basePath") {
        val itemType = O::class.createType()
        router.apply {
            getDefault()
            getSingle(getParamName)
            postDefault(itemType)
            putDefault(itemType)
            deleteDefault(deleteParamName)
        }
    }

    webSocket("/updates") {
        router.apply {
            webSocketDefault()
        }
    }
}
