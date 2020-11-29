package serialCabinet.itemCategories

import generics.IdTable
import org.jetbrains.exposed.sql.ReferenceOption
import serialCabinet.category.SerialCategoriesTable
import serialCabinet.electronic.ElectronicsTable

object SerialItemCategoriesTable : IdTable() {
    val categoryId = reference("categoryId", SerialCategoriesTable.id, ReferenceOption.CASCADE)
    val itemId = reference("itemId", ElectronicsTable.id, ReferenceOption.CASCADE)
}
