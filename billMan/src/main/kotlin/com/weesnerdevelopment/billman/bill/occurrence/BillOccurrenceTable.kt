package com.weesnerdevelopment.billman.bill.occurrence

import com.weesnerdevelopment.auth.user.UserTable
import com.weesnerdevelopment.billman.bill.BillTable
import generics.GenericTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.transactions.transaction

object BillOccurrenceTable : UUIDTable(), GenericTable {
    val owner = reference("owner", UserTable, ReferenceOption.CASCADE)
    val amount = varchar("amount", 255)
    val amountLeft = varchar("amountLeft", 255)
    val bill = reference("bill", BillTable, ReferenceOption.CASCADE)
    val dueDate = long("payDate")
    val every = varchar("every", 255)
    override val dateCreated = long("dateCreated")
    override val dateUpdated = long("dateUpdated")

    fun <T> action(event: BillOccurrenceTable.() -> T) = transaction { event() }
}