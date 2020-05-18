package billCategories

import categories.CategoriesService
import dbQuery
import generics.GenericService
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class BillCategoriesService(
    private val categoriesService: CategoriesService
) : GenericService<BillCategory, BillCategoriesTable>(
    BillCategoriesTable
) {
    suspend fun getByBill(id: Int) =
        dbQuery { table.select { (BillCategoriesTable.billId eq id) }.mapNotNull { to(it) } }.mapNotNull {
            categoriesService.getSingle { categoriesService.table.id eq it.categoryId }
        }

    suspend fun deleteForBill(billId: Int) = dbQuery {
        table.select { (table.billId eq billId) }.mapNotNull { to(it).id }
    }.forEach { delete(it) { table.id eq it } }

    override suspend fun to(row: ResultRow) = BillCategory(
        id = row[BillCategoriesTable.id],
        billId = row[BillCategoriesTable.billId],
        categoryId = row[BillCategoriesTable.categoryId],
        dateCreated = row[BillCategoriesTable.dateCreated],
        dateUpdated = row[BillCategoriesTable.dateUpdated]
    )

    override fun UpdateBuilder<Int>.assignValues(item: BillCategory) {
        this[BillCategoriesTable.billId] = item.billId
        this[BillCategoriesTable.categoryId] = item.categoryId
    }
}
