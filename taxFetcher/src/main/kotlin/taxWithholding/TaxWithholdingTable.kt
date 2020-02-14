package taxWithholding

import generics.IdTable

object TaxWithholdingTable : IdTable() {
    val year = integer("year").primaryKey()
    val type = varchar("type", 255)
    val payPeriod = varchar("payPeriod", 255)
    val amount = double("amount")
}