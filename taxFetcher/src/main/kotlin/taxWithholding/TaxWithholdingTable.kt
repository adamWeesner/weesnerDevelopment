package taxWithholding

import generics.HistoricTable
import generics.IdTable
import history.HistoryTable
import org.jetbrains.exposed.sql.ReferenceOption

object TaxWithholdingTable : IdTable(), HistoricTable {
    val year = integer("year").uniqueIndex()
    val type = varchar("type", 255)
    val payPeriod = varchar("payPeriod", 255)
    val amount = double("amount")
    override val history = reference("historyId", HistoryTable.id, ReferenceOption.CASCADE).nullable()
}
