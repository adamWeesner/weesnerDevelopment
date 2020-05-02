package medicare

import generics.HistoricTable
import generics.IdTable
import history.HistoryTable

object MedicareLimitsTable : IdTable(), HistoricTable {
    val year = integer("year") references MedicareTable.year
    val maritalStatus = varchar("maritalStatus", 255)
    val amount = integer("amount")
    override val history = (integer("historyId") references HistoryTable.id).nullable()
}
