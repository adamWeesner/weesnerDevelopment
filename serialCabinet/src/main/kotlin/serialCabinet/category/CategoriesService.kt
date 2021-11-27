package serialCabinet.category

import BaseService
import com.weesnerdevelopment.shared.serialCabinet.Category
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class CategoriesService : BaseService<SerialCategoriesTable, Category>(
    SerialCategoriesTable
) {
    override val SerialCategoriesTable.connections: Join?
        get() = null

    override suspend fun toItem(row: ResultRow) = Category(
        row[table.id],
        row[table.name],
        row[table.description],
        row[table.dateCreated],
        row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: Category) {
        this[table.name] = item.name
        this[table.description] = item.description
    }
}
