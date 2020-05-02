package billCategories

import bills.BillsTable
import categories.CategoriesTable
import generics.HistoricTable
import generics.IdTable
import history.HistoryTable

object BillCategoriesTable : IdTable(), HistoricTable {
    val categoryId = integer("categoryId") references CategoriesTable.id
    val billId = integer("billId") references BillsTable.id
    override val history = (integer("historyId") references HistoryTable.id).nullable()
}
