package com.weesnerdevelopment.billman.bill.occurrence

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class BillOccurrenceSharedUsersDao(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<BillOccurrenceSharedUsersDao>(BillOccurrenceSharedUsersTable) {
        fun <T> action(event: Companion.() -> T) = transaction { event() }
    }

    var user by BillOccurrenceSharedUsersTable.user
    var occurrence by BillOccurrenceSharedUsersTable.occurrence

    fun <T> action(event: BillOccurrenceSharedUsersDao.() -> T) = transaction { event() }
}

fun BillOccurrenceSharedUsersDao.toUser(): String = user

fun SizedIterable<BillOccurrenceSharedUsersDao>.toUsers(): List<String> = map {
    it.toUser()
}
