package com.weesnerdevelopment.billman.bill.occurrence.payment

import com.weesnerdevelopment.auth.user.UserDao
import com.weesnerdevelopment.auth.user.toUser
import com.weesnerdevelopment.history.HistoryDao
import com.weesnerdevelopment.history.toHistories
import com.weesnerdevelopment.shared.billMan.Payment
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class PaymentDao(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<PaymentDao>(PaymentTable) {
        fun <T> action(event: Companion.() -> T) = transaction { event() }
    }

    val uuid by PaymentTable.id
    var owner by UserDao referencedOn PaymentTable.owner
    var occurrence by PaymentTable.occurrence
    var amount by PaymentTable.amount
    var dateCreated by PaymentTable.dateCreated
    var dateUpdated by PaymentTable.dateUpdated
    val history by HistoryDao via PaymentHistoryTable
}

fun PaymentDao.toPayment(): Payment = Payment(
    uuid = uuid.value.toString(),
    owner = owner.toUser(),
    amount = amount,
    history = history.toHistories(),
    dateCreated = dateCreated,
    dateUpdated = dateUpdated
)

fun SizedIterable<PaymentDao>.toPayments(): List<Payment> = map {
    it.toPayment()
}