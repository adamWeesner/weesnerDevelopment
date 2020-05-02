package medicare

import generics.HistoricTable
import generics.IdTable
import history.HistoryTable

object MedicareTable : IdTable(), HistoricTable {
    val year = integer("year").primaryKey()
    val percent = double("percent")
    val additionalPercent = double("additionalPercent")
    override val history = (integer("historyId") references HistoryTable.id).nullable()
}
