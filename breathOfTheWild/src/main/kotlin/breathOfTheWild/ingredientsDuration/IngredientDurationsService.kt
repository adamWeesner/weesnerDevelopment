package breathOfTheWild.ingredientsDuration

import BaseService
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class IngredientDurationsService : BaseService<IngredientDurationsTable, IngredientDuration>(
    IngredientDurationsTable
) {
    override val IngredientDurationsTable.connections: Join?
        get() = null

    suspend fun getForIngredient(id: Int) = getAll {
        IngredientDurationsTable.ingredientId eq id
    }?.mapNotNull {
        toItem(it).duration
    }


    override suspend fun toItem(row: ResultRow) = IngredientDuration(
        id = row[table.id],
        duration = row[IngredientDurationsTable.duration],
        ingredientId = row[IngredientDurationsTable.ingredientId],
        dateCreated = row[table.dateCreated],
        dateUpdated = row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: IngredientDuration) {
        this[IngredientDurationsTable.duration] = item.duration
        this[IngredientDurationsTable.ingredientId] = item.ingredientId
    }
}
