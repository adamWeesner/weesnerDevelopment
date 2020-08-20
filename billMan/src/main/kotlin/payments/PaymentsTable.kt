package payments

import auth.UsersTable
import generics.HistoricTable
import generics.IdTable
import history.HistoryTable
import occurrences.BillOccurrencesTable
import org.jetbrains.exposed.sql.ReferenceOption

object PaymentsTable : IdTable(), HistoricTable {
    val ownerId = reference("ownerId", UsersTable.uuid, ReferenceOption.CASCADE)
    val occurrenceId = reference("occurrenceId", BillOccurrencesTable.id, ReferenceOption.CASCADE)
    val amount = varchar("amount", 255)
    override val history = reference("historyId", HistoryTable.id, ReferenceOption.CASCADE).nullable()
}
