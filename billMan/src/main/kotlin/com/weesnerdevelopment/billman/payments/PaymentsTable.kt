package com.weesnerdevelopment.billman.payments

import auth.UsersTable
import com.weesnerdevelopment.billman.occurrences.BillOccurrencesTable
import generics.HistoricTable
import generics.IdTable
import history.HistoryTable
import org.jetbrains.exposed.sql.ReferenceOption

object PaymentsTable : IdTable(), HistoricTable {
    val ownerId = reference("ownerId", UsersTable.uuid, ReferenceOption.CASCADE)
    val occurrenceId = reference("occurrenceId", BillOccurrencesTable.id, ReferenceOption.CASCADE)
    val amount = varchar("amount", 255)
    override val history = reference("historyId", HistoryTable.id, ReferenceOption.CASCADE).nullable()
}
