package medicare

import generics.IdTable

object MedicareTable : IdTable() {
    val year = integer("year").primaryKey()
    val percent = double("percent")
    val additionalPercent = double("additionalPercent")
}