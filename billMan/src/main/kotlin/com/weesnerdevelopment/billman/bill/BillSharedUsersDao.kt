package com.weesnerdevelopment.billman.bill

import com.weesnerdevelopment.businessRules.tryTransaction
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SizedIterable
import java.util.*

class BillSharedUsersDao(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<BillSharedUsersDao>(BillSharedUsersTable) {
        fun <T> action(event: Companion.() -> T) = tryTransaction(event)
    }

    var user by BillSharedUsersTable.user
    var bill by BillSharedUsersTable.bill

    fun <T> action(event: BillSharedUsersDao.() -> T) = tryTransaction(event)
}

fun BillSharedUsersDao.toUser(): String? = action {
    user
}

fun SizedIterable<BillSharedUsersDao>.toUsers(): List<String> = BillSharedUsersDao.action {
    mapNotNull {
        it.toUser()
    }
} ?: emptyList()
