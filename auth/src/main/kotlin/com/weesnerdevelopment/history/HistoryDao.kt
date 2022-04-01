package com.weesnerdevelopment.history

import com.weesnerdevelopment.auth.user.UserDao
import com.weesnerdevelopment.auth.user.toUser
import com.weesnerdevelopment.businessRules.tryTransaction
import com.weesnerdevelopment.shared.base.History
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SizedIterable
import java.util.*

class HistoryDao(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<HistoryDao>(HistoryTable) {
        fun <T> action(event: Companion.() -> T) = tryTransaction { event() }
    }

    var field by HistoryTable.field
    var oldValue by HistoryTable.oldValue
    var newValue by HistoryTable.newValue
    var updatedBy by HistoryTable.updatedBy
    var dateCreated by HistoryTable.dateCreated
    var dateUpdated by HistoryTable.dateUpdated
}

fun HistoryDao.toHistory(): History = History(
    field = field,
    oldValue = oldValue,
    newValue = newValue,
    updatedBy = updatedBy.let { UserDao.action { get(it).toUser() }!! },
    dateCreated = dateCreated,
    dateUpdated = dateUpdated
)

fun SizedIterable<HistoryDao>.toHistories(): List<History> = map {
    it.toHistory()
}
