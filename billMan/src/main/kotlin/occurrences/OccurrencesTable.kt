package occurrences

import auth.UsersTable
import bills.BillsTable
import generics.HistoricTable
import generics.IdTable
import history.HistoryTable

object OccurrencesTable : IdTable(), HistoricTable {
    val ownerId = varchar("ownerId", 255) references UsersTable.uuid
    val amount = varchar("amount", 255)
    val itemId = integer("itemId") references BillsTable.id
    val dueDate = long("dueDate")
    val amountLeft = varchar("amountLeft", 255)
    val every = varchar("every", 255)
    override val history = (integer("historyId") references HistoryTable.id).nullable()
}
