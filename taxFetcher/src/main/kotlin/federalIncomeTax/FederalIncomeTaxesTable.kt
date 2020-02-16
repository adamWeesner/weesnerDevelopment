package federalIncomeTax

import generics.IdTable

object FederalIncomeTaxesTable : IdTable() {
    val year = integer("year").primaryKey()
    val maritalStatus = varchar("maritalStatus", 255)
    val payPeriod = varchar("payPeriod", 255)
    val over = double("over")
    val notOver = double("notOver")
    val plus = double("plus")
    val percent = double("percent")
    val nonTaxable = double("nonTaxable")
}
