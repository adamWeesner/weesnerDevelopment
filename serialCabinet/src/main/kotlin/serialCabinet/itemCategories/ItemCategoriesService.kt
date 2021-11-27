package serialCabinet.itemCategories

import BaseService
import com.weesnerdevelopment.shared.base.InvalidAttributeException
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import serialCabinet.category.CategoriesService

class ItemCategoriesService(
    private val categoriesService: CategoriesService
) : BaseService<SerialItemCategoriesTable, ItemCategories>(
    SerialItemCategoriesTable
) {
    override val SerialItemCategoriesTable.connections: Join?
        get() = null

    suspend fun getFor(id: Int) = getAll {
        SerialItemCategoriesTable.itemId eq id
    }?.map {
        val categoryId = toItem(it).categoryId
        categoriesService.get { categoriesService.table.id eq categoryId }
            ?: throw InvalidAttributeException("Category")
    } ?: throw InvalidAttributeException("Categories")

    override suspend fun toItem(row: ResultRow) = ItemCategories(
        row[table.id],
        row[table.categoryId],
        row[table.itemId],
        row[table.dateCreated],
        row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: ItemCategories) {
        this[table.categoryId] = item.categoryId
        this[table.itemId] = item.itemId
    }
}
