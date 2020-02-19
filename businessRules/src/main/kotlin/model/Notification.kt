package model

import com.squareup.moshi.JsonClass

/**
 * The change type of the [Notification].
 */
enum class ChangeType { Create, Update, Delete, Error }

/**
 * Notification for websocket connections to keep track of what is changed and what change was made.
 */
@JsonClass(generateAdapter = true)
data class Notification<T>(
    val type: ChangeType,
    val id: Int,
    val entity: T
)