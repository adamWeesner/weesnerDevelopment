package com.weesnerdevelopment.billman.bill.occurrence.payment

import com.weesnerdevelopment.billman.bill.occurrence.BillOccurrenceTable
import com.weesnerdevelopment.businessRules.tryTransaction
import generics.GenericTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption

object PaymentTable : UUIDTable(), GenericTable {
    val owner = varchar("owner", 36)
    val occurrence = reference("occurrence", BillOccurrenceTable, ReferenceOption.CASCADE)
    val amount = varchar("amount", 255)
    override val dateCreated = long("dateCreated")
    override val dateUpdated = long("dateUpdated")

    fun <T> action(event: PaymentTable.() -> T) = tryTransaction(event)
}