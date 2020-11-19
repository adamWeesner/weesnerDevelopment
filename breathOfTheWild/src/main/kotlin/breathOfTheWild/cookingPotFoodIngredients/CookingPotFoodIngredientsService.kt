package breathOfTheWild.cookingPotFoodIngredients

import BaseService
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import shared.base.InvalidAttributeException

class CookingPotFoodIngredientsService : BaseService<CookingPotFoodIngredientsTable, CookingPotFoodIngredient>(
    CookingPotFoodIngredientsTable
) {
    override val CookingPotFoodIngredientsTable.connections: Join?
        get() = null

    suspend fun getFor(id: Int) = getAll {
        CookingPotFoodIngredientsTable.itemId eq id
    }?.map { toItem(it).ingredient } ?: throw InvalidAttributeException("CookingPotFoodIngredients")

    override suspend fun toItem(row: ResultRow) = CookingPotFoodIngredient(
        row[table.id],
        row[table.ingredient],
        row[table.itemId],
        row[table.dateCreated],
        row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: CookingPotFoodIngredient) {
        this[table.ingredient] = item.ingredient
        this[table.itemId] = item.itemId
    }
}
