package generics

import io.ktor.routing.Route
import io.ktor.routing.route
import io.ktor.websocket.webSocket
import shared.base.GenericItem
import kotlin.reflect.full.createType

/**
 * Route at /[basePath] using the given [router].
 */
inline fun <reified O : GenericItem, T : IdTable> Route.route(
    basePath: String,
    router: GenericRouter<O, T>,
    crossinline customRoutes: Route.(router: GenericRouter<O, T>) -> Unit = {}
) {
    route("/$basePath") {
        router.apply {
            itemType = O::class.createType()
            getDefault()
            getSingle()
            postDefault()
            putDefault()
            deleteDefault()
        }
        customRoutes(router)
    }

    webSocket("/updates") {
        router.apply {
            webSocketDefault()
        }
    }
}
