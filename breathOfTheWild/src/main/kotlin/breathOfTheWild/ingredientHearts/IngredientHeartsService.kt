package breathOfTheWild.ingredientHearts

import BaseService
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.base.InvalidAttributeException

class IngredientHeartsService : BaseService<IngredientHeartsTable, IngredientHeart>(
    IngredientHeartsTable
) {
    override val IngredientHeartsTable.connections: Join?
        get() = null
        
    suspend fun getFor(id: Int) = getAll {
        IngredientHeartsTable.itemId eq id
    }?.map { toItem(it) }?: throw InvalidAttributeException("IngredientHearts")

    override suspend fun toItem(row: ResultRow) = IngredientHeart(
        row[table.id],
        row[table.imageId],
        row[table.itemId],
        row[table.dateCreated],
        row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: IngredientHeart) {
        this[table.imageId] = item.imageId
        this[table.itemId] = item.itemId
    }
}
