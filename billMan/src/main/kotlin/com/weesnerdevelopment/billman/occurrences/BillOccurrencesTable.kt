package com.weesnerdevelopment.billman.occurrences

import auth.UsersTable
import com.weesnerdevelopment.billman.bills.BillsTable
import generics.HistoricTable
import generics.IdTable
import history.HistoryTable
import org.jetbrains.exposed.sql.ReferenceOption

object BillOccurrencesTable : IdTable(), OccurrenceTable, HistoricTable {
    override val ownerId = reference("ownerId", UsersTable.uuid, ReferenceOption.CASCADE)
    override val amount = varchar("amount", 255)
    override val itemId = reference("itemId", BillsTable.id, ReferenceOption.CASCADE)
    override val dueDate = long("dueDate")
    val amountLeft = varchar("amountLeft", 255)
    override val every = varchar("every", 255)
    override val history = reference("historyId", HistoryTable.id, ReferenceOption.CASCADE).nullable()
}
