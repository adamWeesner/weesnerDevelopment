package breathOfTheWild.roastedFoodIngredients

import BaseService
import com.weesnerdevelopment.shared.base.InvalidAttributeException
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class RoastedFoodIngredientsService : BaseService<RoastedFoodIngredientsTable, RoastedFoodIngredient>(
    RoastedFoodIngredientsTable
) {
    override val RoastedFoodIngredientsTable.connections: Join?
        get() = null
        
    suspend fun getFor(id: Int) = getAll {
        RoastedFoodIngredientsTable.itemId eq id
    }?.map { toItem(it).ingredient }?: throw InvalidAttributeException("RoastedFoodIngredients")

    override suspend fun toItem(row: ResultRow) = RoastedFoodIngredient(
        row[table.id],
        row[table.ingredient],
        row[table.itemId],
        row[table.dateCreated],
        row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: RoastedFoodIngredient) {
        this[table.ingredient] = item.ingredient
        this[table.itemId] = item.itemId
    }
}
