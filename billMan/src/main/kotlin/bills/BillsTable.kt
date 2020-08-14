package bills

import auth.UsersTable
import generics.HistoricTable
import generics.IdTable
import history.HistoryTable
import org.jetbrains.exposed.sql.ReferenceOption

object BillsTable : IdTable(), HistoricTable {
    val ownerId = reference("ownerId", UsersTable.uuid, ReferenceOption.CASCADE)
    val name = varchar("name", 255)
    val amount = varchar("amount", 255)
    val varyingAmount = bool("varyingAmount")
    val payoffAmount = varchar("payoffAmount", 255).nullable()
    override val history = reference("historyId", HistoryTable.id, ReferenceOption.CASCADE).nullable()
}
