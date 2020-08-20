package colors

import bills.BillsTable
import generics.HistoricTable
import generics.IdTable
import history.HistoryTable
import org.jetbrains.exposed.sql.ReferenceOption

object ColorsTable : IdTable(), HistoricTable {
    val billId = reference("billId", BillsTable.id, ReferenceOption.CASCADE)
    val red = integer("red")
    val green = integer("green")
    val blue = integer("blue")
    val alpha = integer("alpha")
    override val history = reference("historyId", HistoryTable.id, ReferenceOption.CASCADE).nullable()
}
