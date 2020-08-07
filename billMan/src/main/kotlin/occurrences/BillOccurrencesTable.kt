package occurrences

import auth.UsersTable
import bills.BillsTable
import generics.HistoricTable
import generics.IdTable
import history.HistoryTable

object BillOccurrencesTable : IdTable(), OccurrenceTable, HistoricTable {
    override val ownerId = varchar("ownerId", 255) references UsersTable.uuid
    override val amount = varchar("amount", 255)
    override val itemId = integer("itemId") references BillsTable.id
    override val dueDate = long("dueDate")
    val amountLeft = varchar("amountLeft", 255)
    override val every = varchar("every", 255)
    override val history = (integer("historyId") references HistoryTable.id).nullable()
}
