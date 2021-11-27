package billCategories

import com.weesnerdevelopment.billman.bills.BillsTable
import com.weesnerdevelopment.billman.categories.CategoriesTable
import generics.IdTable
import org.jetbrains.exposed.sql.ReferenceOption

object BillCategoriesTable : IdTable() {
    val categoryId = reference("categoryId", CategoriesTable.id, ReferenceOption.CASCADE)
    val billId = reference("billId", BillsTable.id, ReferenceOption.CASCADE)
}
