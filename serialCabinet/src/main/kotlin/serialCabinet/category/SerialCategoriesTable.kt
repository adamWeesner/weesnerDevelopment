package serialCabinet.category

import generics.IdTable

object SerialCategoriesTable : IdTable() {
    val name = varchar("name", 255)
    val description = text("description")
}
