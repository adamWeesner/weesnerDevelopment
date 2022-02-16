package com.weesnerdevelopment.billman.income.occurrence

import com.weesnerdevelopment.billman.income.IncomeTable
import com.weesnerdevelopment.businessRules.tryTransaction
import generics.GenericTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption

object IncomeOccurrenceTable : UUIDTable(), GenericTable {
    val owner = varchar("owner", 36)
    val amount = varchar("amount", 255)
    val income = reference("income", IncomeTable, ReferenceOption.CASCADE)
    val dueDate = long("payDate")
    val every = varchar("every", 255)
    override val dateCreated = long("dateCreated")
    override val dateUpdated = long("dateUpdated")

    fun <T> action(event: IncomeOccurrenceTable.() -> T) = tryTransaction(event)
}