package com.weesnerdevelopment.billman.bill.occurrence

import com.weesnerdevelopment.businessRules.tryTransaction
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SizedIterable
import java.util.*

class BillOccurrenceSharedUsersDao(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<BillOccurrenceSharedUsersDao>(BillOccurrenceSharedUsersTable) {
        fun <T> action(event: Companion.() -> T) = tryTransaction(event)
    }

    var user by BillOccurrenceSharedUsersTable.user
    var occurrence by BillOccurrenceSharedUsersTable.occurrence

    fun <T> action(event: BillOccurrenceSharedUsersDao.() -> T) = tryTransaction(event)
}

fun BillOccurrenceSharedUsersDao.toUser(): String? = action {
    user
}

fun SizedIterable<BillOccurrenceSharedUsersDao>.toUsers(): List<String> = BillOccurrenceSharedUsersDao.action {
    mapNotNull {
        it.toUser()
    }
} ?: emptyList()
