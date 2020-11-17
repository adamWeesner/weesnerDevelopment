package breathOfTheWild.otherFoodIngredients

import BaseService
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.base.InvalidAttributeException

class OtherFoodIngredientsService : BaseService<OtherFoodIngredientsTable, OtherFoodIngredient>(
    OtherFoodIngredientsTable
) {
    override val OtherFoodIngredientsTable.connections: Join?
        get() = null
        
    suspend fun getFor(id: Int) = getAll {
        OtherFoodIngredientsTable.itemId eq id
    }?.map { toItem(it).ingredient }?: throw InvalidAttributeException("OtherFoodIngredients")

    override suspend fun toItem(row: ResultRow) = OtherFoodIngredient(
        row[table.id],
        row[table.ingredient],
        row[table.itemId],
        row[table.dateCreated],
        row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: OtherFoodIngredient) {
        this[table.ingredient] = item.ingredient
        this[table.itemId] = item.itemId
    }
}
