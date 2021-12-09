package com.weesnerdevelopment.billman.income.occurrence

import com.weesnerdevelopment.auth.user.UserDao
import com.weesnerdevelopment.auth.user.toUser
import com.weesnerdevelopment.billman.income.IncomeDao
import com.weesnerdevelopment.billman.income.toIncome
import com.weesnerdevelopment.history.HistoryDao
import com.weesnerdevelopment.history.toHistories
import com.weesnerdevelopment.shared.billMan.IncomeOccurrence
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class IncomeOccurrenceDao(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<IncomeOccurrenceDao>(IncomeOccurrenceTable) {
        fun <T> action(event: Companion.() -> T) = transaction { event() }
    }

    val uuid by IncomeOccurrenceTable.id
    var owner by UserDao referencedOn IncomeOccurrenceTable.owner
    var amount by IncomeOccurrenceTable.amount
    var income by IncomeDao referencedOn IncomeOccurrenceTable.income
    var dueDate by IncomeOccurrenceTable.dueDate
    var every by IncomeOccurrenceTable.every
    var dateCreated by IncomeOccurrenceTable.dateCreated
    var dateUpdated by IncomeOccurrenceTable.dateUpdated
    val history by HistoryDao via IncomeOccurrenceHistoryTable

    fun <T> action(event: IncomeOccurrenceDao.() -> T) = transaction { event() }
}

fun SizedIterable<IncomeOccurrenceDao>.toCategories(): List<IncomeOccurrence> = map {
    it.toIncomeOccurrence()
}

fun IncomeOccurrenceDao.toIncomeOccurrence(): IncomeOccurrence = IncomeOccurrence(
    uuid = uuid.value.toString(),
    owner = owner.toUser(),
    sharedUsers = emptyList(), // this shouldnt be here...
    amountLeft = "", // this shouldnt be here...
    payments = emptyList(), // this shouldnt be here...
    itemId = income.toIncome().uuid!!,
    dueDate = dueDate,
    amount = amount,
    every = every,
    history = history.toHistories(),
    dateCreated = dateCreated,
    dateUpdated = dateUpdated
)
