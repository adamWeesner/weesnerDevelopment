package com.weesnerdevelopment.billman.bill.occurrence

import com.weesnerdevelopment.billman.bill.BillDao
import com.weesnerdevelopment.billman.bill.occurrence.payment.PaymentDao
import com.weesnerdevelopment.billman.bill.occurrence.payment.toPayments
import com.weesnerdevelopment.billman.bill.toBill
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

    var owner by BillOccurrenceTable.owner
    var amount by BillOccurrenceTable.amount
    var amountLeft by BillOccurrenceTable.amountLeft
    var bill by BillDao referencedOn BillOccurrenceTable.bill
    var payments by PaymentDao via BillOccurrencePaymentsTable
    var dueDate by BillOccurrenceTable.dueDate
    var every by BillOccurrenceTable.every
    var dateCreated by BillOccurrenceTable.dateCreated
    var dateUpdated by BillOccurrenceTable.dateUpdated
    //val history by HistoryDao via BillOccurrenceHistoryTable

    fun <T> action(event: BillOccurrenceDao.() -> T) = transaction { event() }
}

fun BillOccurrenceDao.toBillOccurrence(): BillOccurrence {
    val sharedUsers = BillOccurrenceSharedUsersDao.action {
        find {
            BillOccurrenceSharedUsersTable.occurrence eq this@toBillOccurrence.id
        }
    }

    return BillOccurrence(
        uuid = id.value.toString(),
        owner = owner,
        sharedUsers = sharedUsers.toUsers(),
        amountLeft = amountLeft,
        payments = payments.toPayments(),
        itemId = bill.toBill().uuid!!,
        dueDate = dueDate,
        amount = amount,
        every = every,
        //history = history.toHistories(),
        dateCreated = dateCreated,
        dateUpdated = dateUpdated
    )
}

fun SizedIterable<BillOccurrenceDao>.toBillOccurrences(): List<BillOccurrence> = map {
    it.toBillOccurrence()
}