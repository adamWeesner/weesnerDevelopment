package com.weesnerdevelopment.billman.color

import com.weesnerdevelopment.businessRules.tryTransaction
import com.weesnerdevelopment.shared.billMan.Color
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class ColorDao(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ColorDao>(ColorTable) {
        fun <T> action(event: Companion.() -> T) = tryTransaction(event)
    }

    val uuid by ColorTable.id
    var red by ColorTable.red
    var green by ColorTable.green
    var blue by ColorTable.blue
    var alpha by ColorTable.alpha
    var dateCreated by ColorTable.dateCreated
    var dateUpdated by ColorTable.dateUpdated
//    val history by HistoryDao via ColorHistoryTable

    fun <T> action(event: ColorDao.() -> T) = tryTransaction(event)
}

fun ColorDao.toColor(): Color? = action {
    Color(
        uuid = uuid.value.toString(),
        red = red,
        green = green,
        blue = blue,
        alpha = alpha,
//    history = history.toHistories(),
        dateCreated = dateCreated,
        dateUpdated = dateUpdated
    )
}