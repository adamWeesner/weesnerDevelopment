package colors

import bills.BillsTable
import generics.HistoricTable
import generics.IdTable
import history.HistoryTable

object ColorsTable : IdTable(), HistoricTable {
    val billId = integer("billId") references BillsTable.id
    val red = integer("red")
    val green = integer("green")
    val blue = integer("blue")
    val alpha = integer("alpha")
    override val history = (integer("historyId") references HistoryTable.id).nullable()
}
