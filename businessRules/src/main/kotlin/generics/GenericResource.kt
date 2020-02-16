package generics

import io.ktor.routing.Route
import io.ktor.routing.route
import io.ktor.websocket.webSocket
import kotlin.reflect.full.createType

/**
 * ## Routes for the given item type [O] in the table [T]:
 *
 * > GET /[basePath] --> get all items in the database
 *
 * > GET /[basePath]/{id} --> get one item instance by `id`
 *
 * > POST /[basePath] --> add a new item by providing a JSON object with all of the non-nullable values for the item,
 * > generally `id`, `dateCreated`, and `dateUpdated` can be omitted if you choose, but all other item values should be
 * > provided example for [SocialSecurity]:
 * ```json
 * {
 *   "year": 2016,
 *   "percent": 1.45,
 *   "limit": 125000
 * }
 * ```
 *
 * > PUT /[basePath] --> update an existing items values. Pass in the `id` along with all other non-nullable fields for
 * > the item, in the JSON request to determine which record to update, passing no `id` creates a new item
 *
 * > DELETE /[basePath]/{id} --> delete the item with the specified id
 *
 *
 * > WS /updates --> returns Notification instances containing the change `type`, `id` and `entity` (if applicable) e.g:
 * ```json
 * {
 *   "type": "CREATE",
 *   "id": 12,
 *   "entity": {
 *       "id": 12,
 *       "name": "widget1",
 *       "quantity": 5,
 *       "dateCreated": 1533583858169
 *       "dateUpdated": 1533583858169
 *   }
 * }
 * ```
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
