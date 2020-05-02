package taxWithholding

import generics.HistoricTable
import generics.IdTable
import history.HistoryTable

object TaxWithholdingTable : IdTable(), HistoricTable {
    val year = integer("year").primaryKey()
    val type = varchar("type", 255)
    val payPeriod = varchar("payPeriod", 255)
    val amount = double("amount")
    override val history = (integer("historyId") references HistoryTable.id).nullable()
}
