package incomeOccurrences

import auth.UsersTable
import generics.HistoricTable
import generics.IdTable
import history.HistoryTable
import income.IncomeTable
import occurrences.OccurrenceTable

object IncomeOccurrencesTable : IdTable(), OccurrenceTable, HistoricTable {
    override val ownerId = varchar("ownerId", 255) references UsersTable.uuid
    override val amount = varchar("amount", 255)
    override val itemId = integer("itemId") references IncomeTable.id
    override val dueDate = long("payDate")
    override val every = varchar("every", 255)
    override val history = (integer("historyId") references HistoryTable.id).nullable()
}
