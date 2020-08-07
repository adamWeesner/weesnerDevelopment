package payments

import auth.UsersTable
import generics.HistoricTable
import generics.IdTable
import history.HistoryTable
import occurrences.BillOccurrencesTable

object PaymentsTable : IdTable(), HistoricTable {
    val ownerId = varchar("ownerId", 255) references UsersTable.uuid
    val occurrenceId = integer("occurrenceId") references BillOccurrencesTable.id
    val amount = varchar("amount", 255)
    override val history = (integer("historyId") references HistoryTable.id).nullable()
}
