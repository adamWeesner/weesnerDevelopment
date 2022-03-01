package com.weesnerdevelopment.billman.bill.occurrence.payment

import com.weesnerdevelopment.businessRules.tryTransaction
import com.weesnerdevelopment.shared.billMan.Payment
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SizedIterable
import java.util.*

class PaymentDao(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<PaymentDao>(PaymentTable) {
        fun <T> action(event: Companion.() -> T) = tryTransaction(event)
    }

    val uuid by PaymentTable.id
    var owner by PaymentTable.owner
    var occurrence by PaymentTable.occurrence
    var amount by PaymentTable.amount
    var dateCreated by PaymentTable.dateCreated
    var dateUpdated by PaymentTable.dateUpdated
    //val history by HistoryDao via PaymentHistoryTable

    fun <T> action(event: PaymentDao.() -> T) = tryTransaction(event)
}

fun PaymentDao.toPayment(): Payment? = action {
    Payment(
        uuid = uuid.value.toString(),
        owner = owner,
        amount = amount,
        //history = history.toHistories(),
        dateCreated = dateCreated,
        dateUpdated = dateUpdated
    )
}

fun SizedIterable<PaymentDao>.toPayments(): List<Payment> = PaymentDao.action {
    mapNotNull {
        it.toPayment()
    }
} ?: emptyList()
