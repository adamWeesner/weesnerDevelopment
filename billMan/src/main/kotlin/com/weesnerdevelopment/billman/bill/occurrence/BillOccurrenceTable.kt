package com.weesnerdevelopment.billman.bill.occurrence

import com.weesnerdevelopment.billman.bill.BillTable
import com.weesnerdevelopment.businessRules.tryTransaction
import generics.GenericTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption

object BillOccurrenceTable : UUIDTable(), GenericTable {
    val owner = varchar("owner", 36)
    val amount = varchar("amount", 255)
    val amountLeft = varchar("amountLeft", 255)
    val bill = reference("bill", BillTable, ReferenceOption.CASCADE)
    val dueDate = long("payDate")
    val every = varchar("every", 255)
    override val dateCreated = long("dateCreated")
    override val dateUpdated = long("dateUpdated")

    fun <T> action(event: BillOccurrenceTable.() -> T) = tryTransaction(event)
}