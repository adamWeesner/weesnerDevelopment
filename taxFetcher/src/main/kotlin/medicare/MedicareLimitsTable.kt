package medicare

import generics.IdTable

object MedicareLimitsTable : IdTable() {
    val year = (integer("year") references MedicareTable.year)
    val maritalStatus = varchar("maritalStatus", 255)
    val amount = integer("amount")
}
