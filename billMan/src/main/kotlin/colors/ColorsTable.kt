package colors

import bills.BillsTable
import generics.IdTable

object ColorsTable : IdTable() {
    val billId = integer("billId") references BillsTable.id
    val red = integer("red")
    val green = integer("green")
    val blue = integer("blue")
    val alpha = integer("alpha")
}
