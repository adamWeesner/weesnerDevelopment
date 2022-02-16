package com.weesnerdevelopment.billman.bill

import com.weesnerdevelopment.billman.category.CategoryDao
import com.weesnerdevelopment.billman.category.toCategories
import com.weesnerdevelopment.billman.color.ColorDao
import com.weesnerdevelopment.billman.color.toColor
import com.weesnerdevelopment.shared.billMan.Bill
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class BillDao(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<BillDao>(BillTable) {
        fun <T> action(event: Companion.() -> T) = transaction { event() }
    }

    var owner by BillTable.owner
    var name by BillTable.name
    var amount by BillTable.amount
    var varyingAmount by BillTable.varyingAmount
    var payoffAmount by BillTable.payoffAmount
    var color by ColorDao referencedOn BillTable.color
    var categories by CategoryDao via BillsCategoriesTable
    var dateCreated by BillTable.dateCreated
    var dateUpdated by BillTable.dateUpdated
//    val history by HistoryDao via BillHistoryTable

    fun <T> action(event: BillDao.() -> T) = transaction { event() }
}

fun BillDao.toBill(): Bill {
    val sharedUsers = BillSharedUsersDao.action {
        find {
            BillSharedUsersTable.bill eq this@toBill.id
        }
    }

    return Bill(
        uuid = id.value.toString(),
        name = name,
        owner = owner,
        amount = amount,
        varyingAmount = varyingAmount,
        payoffAmount = payoffAmount,
        sharedUsers = sharedUsers.toUsers(),
        categories = categories.toCategories(),
        color = color.toColor(),
//    history = history.toHistories(),
        dateCreated = dateCreated,
        dateUpdated = dateUpdated
    )
}

fun SizedIterable<BillDao>.toBills(): List<Bill> = map {
    it.toBill()
}
