package com.weesnerdevelopment.billman.bill

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class BillSharedUsersDao(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<BillSharedUsersDao>(BillSharedUsersTable) {
        fun <T> action(event: Companion.() -> T) = transaction { event() }
    }

    var user by BillSharedUsersTable.user
    var bill by BillSharedUsersTable.bill

    fun <T> action(event: BillSharedUsersDao.() -> T) = transaction { event() }
}

fun BillSharedUsersDao.toUser(): String = user

fun SizedIterable<BillSharedUsersDao>.toUsers(): List<String> = map {
    it.toUser()
}
