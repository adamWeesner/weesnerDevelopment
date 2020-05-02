package billCategories

import HistoryTypes
import categories.CategoriesService
import dbQuery
import generics.GenericService
import history.HistoryService
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class BillCategoriesService(
    private val categoriesService: CategoriesService,
    private val historyService: HistoryService
) : GenericService<BillCategory, BillCategoriesTable>(
    BillCategoriesTable
) {
    suspend fun getByBill(id: Int) =
        dbQuery { table.select { (BillCategoriesTable.billId eq id) }.mapNotNull { to(it) } }.mapNotNull {
            categoriesService.getSingle { categoriesService.table.id eq it.categoryId }
        }

    override suspend fun to(row: ResultRow) = BillCategory(
        id = row[BillCategoriesTable.id],
        billId = row[BillCategoriesTable.billId],
        categoryId = row[BillCategoriesTable.categoryId],
        history = historyService.getFor(
            HistoryTypes.Color.name,
            row[BillCategoriesTable.id]
        ),
        dateCreated = row[BillCategoriesTable.dateCreated],
        dateUpdated = row[BillCategoriesTable.dateUpdated]
    )

    override fun UpdateBuilder<Int>.assignValues(item: BillCategory) {
        this[BillCategoriesTable.billId] = item.billId
        this[BillCategoriesTable.categoryId] = item.categoryId
    }
}
