package com.weesnerdevelopment.billman.income

import com.weesnerdevelopment.billman.color.ColorDao
import com.weesnerdevelopment.billman.color.toColor
import com.weesnerdevelopment.businessRules.tryTransaction
import com.weesnerdevelopment.shared.billMan.Income
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SizedIterable
import java.util.*

class IncomeDao(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<IncomeDao>(IncomeTable) {
        fun <T> action(event: Companion.() -> T) = tryTransaction(event)
    }

    var owner by IncomeTable.owner
    var name by IncomeTable.name
    var amount by IncomeTable.amount
    var varyingAmount by IncomeTable.varyingAmount
    var color by ColorDao referencedOn IncomeTable.color
    var dateCreated by IncomeTable.dateCreated
    var dateUpdated by IncomeTable.dateUpdated
//    val history by HistoryDao via IncomeHistoryTable

    fun <T> action(event: IncomeDao.() -> T) = tryTransaction(event)
}

fun IncomeDao.toIncome(): Income? = action {
    Income(
        uuid = id.value.toString(),
        name = name,
        owner = owner,
        amount = amount,
        varyingAmount = varyingAmount,
        color = color.toColor()!!,
//    history = history.toHistories(),
        dateCreated = dateCreated,
        dateUpdated = dateUpdated
    )
}

fun SizedIterable<IncomeDao>.toIncomes(): List<Income> = IncomeDao.action {
    mapNotNull {
        it.toIncome()
    }
} ?: emptyList()
