package model

import com.squareup.moshi.JsonClass

enum class ChangeType { Create, Update, Delete, Error }

@JsonClass(generateAdapter = true)
data class Notification<T>(
    val type: ChangeType,
    val id: Int,
    val entity: T
)