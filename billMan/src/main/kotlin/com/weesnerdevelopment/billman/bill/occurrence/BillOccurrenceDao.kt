package com.weesnerdevelopment.billman.bill.occurrence

import com.weesnerdevelopment.auth.user.UserDao
import com.weesnerdevelopment.auth.user.toUser
import com.weesnerdevelopment.auth.user.toUsers
import com.weesnerdevelopment.billman.bill.BillDao
import com.weesnerdevelopment.billman.bill.occurrence.payment.PaymentDao
import com.weesnerdevelopment.billman.bill.occurrence.payment.toPayments
import com.weesnerdevelopment.billman.bill.toBill
import com.weesnerdevelopment.history.HistoryDao
import com.weesnerdevelopment.history.toHistories
import com.weesnerdevelopment.shared.billMan.BillOccurrence
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class BillOccurrenceDao(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<BillOccurrenceDao>(BillOccurrenceTable) {
        fun <T> action(event: Companion.() -> T) = transaction { event() }
    }

    val uuid by BillOccurrenceTable.id
    var owner by UserDao referencedOn BillOccurrenceTable.owner
    var amount by BillOccurrenceTable.amount
    var amountLeft by BillOccurrenceTable.amountLeft
    var bill by BillDao referencedOn BillOccurrenceTable.bill
    var payments by PaymentDao via BillOccurrencePaymentsTable
    var sharedUsers by UserDao via BillOccurrenceSharedUsersTable
    var dueDate by BillOccurrenceTable.dueDate
    var every by BillOccurrenceTable.every
    var dateCreated by BillOccurrenceTable.dateCreated
    var dateUpdated by BillOccurrenceTable.dateUpdated
    val history by HistoryDao via BillOccurrenceHistoryTable

    fun <T> action(event: BillOccurrenceDao.() -> T) = transaction { event() }
}

fun SizedIterable<BillOccurrenceDao>.toCategories(): List<BillOccurrence> = map {
    it.toBillOccurrence()
}

fun BillOccurrenceDao.toBillOccurrence(): BillOccurrence = BillOccurrence(
    uuid = uuid.value.toString(),
    owner = owner.toUser(),
    sharedUsers = sharedUsers.toUsers(),
    amountLeft = amountLeft,
    payments = payments.toPayments(),
    itemId = bill.toBill().uuid!!,
    dueDate = dueDate,
    amount = amount,
    every = every,
    history = history.toHistories(),
    dateCreated = dateCreated,
    dateUpdated = dateUpdated
)
