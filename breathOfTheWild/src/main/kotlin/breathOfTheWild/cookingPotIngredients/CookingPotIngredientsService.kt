package breathOfTheWild.cookingPotIngredients

import BaseService
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class CookingPotIngredientsService : BaseService<CookingPotIngredientsTable, CookingPotFoodIngredient>(
    CookingPotIngredientsTable
) {
    override val CookingPotIngredientsTable.connections: Join?
        get() = null

    suspend fun getForFood(id: Int) = getAll {
        CookingPotIngredientsTable.cookingPotItemId eq id
    }?.map { toItem(it).name }!!

    override suspend fun toItem(row: ResultRow) = CookingPotFoodIngredient(
        id = row[table.id],
        name = row[CookingPotIngredientsTable.name],
        cookingPotItemId = row[CookingPotIngredientsTable.cookingPotItemId],
        dateCreated = row[table.dateCreated],
        dateUpdated = row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: CookingPotFoodIngredient) {
        this[CookingPotIngredientsTable.name] = item.name
        this[CookingPotIngredientsTable.cookingPotItemId] = item.cookingPotItemId
    }
}
