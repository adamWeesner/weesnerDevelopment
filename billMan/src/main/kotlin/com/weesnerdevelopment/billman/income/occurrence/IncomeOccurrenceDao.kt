package com.weesnerdevelopment.billman.income.occurrence

import com.weesnerdevelopment.billman.income.IncomeDao
import com.weesnerdevelopment.billman.income.toIncome
import com.weesnerdevelopment.businessRules.tryTransaction
import com.weesnerdevelopment.shared.billMan.IncomeOccurrence
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SizedIterable
import java.util.*

class IncomeOccurrenceDao(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<IncomeOccurrenceDao>(IncomeOccurrenceTable) {
        fun <T> action(event: Companion.() -> T) = tryTransaction(event)
    }

    val uuid by IncomeOccurrenceTable.id
    var owner by IncomeOccurrenceTable.owner
    var amount by IncomeOccurrenceTable.amount
    var income by IncomeDao referencedOn IncomeOccurrenceTable.income
    var dueDate by IncomeOccurrenceTable.dueDate
    var every by IncomeOccurrenceTable.every
    var dateCreated by IncomeOccurrenceTable.dateCreated
    var dateUpdated by IncomeOccurrenceTable.dateUpdated
//    val history by HistoryDao via IncomeOccurrenceHistoryTable

    fun <T> action(event: IncomeOccurrenceDao.() -> T) = tryTransaction(event)
}


fun IncomeOccurrenceDao.toIncomeOccurrence(): IncomeOccurrence? = action {
    IncomeOccurrence(
        uuid = uuid.value.toString(),
        owner = owner,
        itemId = income.toIncome()?.uuid!!,
        dueDate = dueDate,
        amount = amount,
        every = every,
//    history = history.toHistories(),
        dateCreated = dateCreated,
        dateUpdated = dateUpdated
    )
}

fun SizedIterable<IncomeOccurrenceDao>.toIncomeOccurrences(): List<IncomeOccurrence> = IncomeOccurrenceDao.action {
    mapNotNull {
        it.toIncomeOccurrence()
    }
} ?: emptyList()
