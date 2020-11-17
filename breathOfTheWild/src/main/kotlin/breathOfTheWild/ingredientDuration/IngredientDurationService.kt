package breathOfTheWild.ingredientDuration

import BaseService
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class IngredientDurationService : BaseService<IngredientDurationTable, IngredientDuration>(
    IngredientDurationTable
) {
    override val IngredientDurationTable.connections: Join?
        get() = null
        
    suspend fun getFor(id: Int) = getAll {
        IngredientDurationTable.itemId eq id
    }?.map { toItem(it).duration }

    override suspend fun toItem(row: ResultRow) = IngredientDuration(
        row[table.id],
        row[table.duration],
        row[table.itemId],
        row[table.dateCreated],
        row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: IngredientDuration) {
        this[table.duration] = item.duration
        this[table.itemId] = item.itemId
    }
}
