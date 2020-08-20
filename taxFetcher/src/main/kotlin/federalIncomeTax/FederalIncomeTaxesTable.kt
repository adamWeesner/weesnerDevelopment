package federalIncomeTax

import generics.HistoricTable
import generics.IdTable
import history.HistoryTable
import org.jetbrains.exposed.sql.ReferenceOption

object FederalIncomeTaxesTable : IdTable(), HistoricTable {
    val year = integer("year").uniqueIndex()
    val maritalStatus = varchar("maritalStatus", 255)
    val payPeriod = varchar("payPeriod", 255)
    val over = double("over")
    val notOver = double("notOver")
    val plus = double("plus")
    val percent = double("percent")
    val nonTaxable = double("nonTaxable")
    override val history = reference("historyId", HistoryTable.id, ReferenceOption.CASCADE).nullable()
}
