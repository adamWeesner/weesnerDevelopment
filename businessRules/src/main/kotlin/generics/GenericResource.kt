package generics

import com.weesnerdevelopment.shared.base.GenericItem
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlin.reflect.full.createType

/**
 * Route at /[router.basePath] using the given [router].
 */
inline fun <reified O : GenericItem, T : IdTable> Route.route(
    router: GenericRouter<O, T>,
    crossinline customRoutes: Route.(router: GenericRouter<O, T>) -> Unit = {}
) {
    route("/${router.basePath}") {
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
