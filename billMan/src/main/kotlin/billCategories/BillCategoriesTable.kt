package billCategories

import bills.BillsTable
import categories.CategoriesTable
import generics.IdTable

object BillCategoriesTable : IdTable() {
    val categoryId = integer("categoryId") references CategoriesTable.id
    val billId = integer("billId") references BillsTable.id
}
