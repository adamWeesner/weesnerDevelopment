package bills

import auth.UsersTable
import generics.HistoricTable
import generics.IdTable
import history.HistoryTable

object BillsTable : IdTable(), HistoricTable {
    val ownerId = varchar("ownerId", 255) references UsersTable.uuid
    val name = varchar("name", 255)
    val amount = varchar("amount", 255)
    val varyingAmount = bool("varyingAmount")
    val payoffAmount = varchar("payoffAmount", 255).nullable()
    override val history = (integer("historyId") references HistoryTable.id).nullable()
}
