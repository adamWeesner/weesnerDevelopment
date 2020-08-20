package medicare

import generics.HistoricTable
import generics.IdTable
import history.HistoryTable
import org.jetbrains.exposed.sql.ReferenceOption

object MedicareTable : IdTable(), HistoricTable {
    val year = integer("year").uniqueIndex()
    val percent = double("percent")
    val additionalPercent = double("additionalPercent")
    override val history = reference("historyId", HistoryTable.id, ReferenceOption.CASCADE).nullable()
}
