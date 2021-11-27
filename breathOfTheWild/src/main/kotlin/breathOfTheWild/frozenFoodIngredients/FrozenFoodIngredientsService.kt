package breathOfTheWild.frozenFoodIngredients

import BaseService
import com.weesnerdevelopment.shared.base.InvalidAttributeException
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder

class FrozenFoodIngredientsService : BaseService<FrozenFoodIngredientsTable, FrozenFoodIngredient>(
    FrozenFoodIngredientsTable
) {
    override val FrozenFoodIngredientsTable.connections: Join?
        get() = null

    suspend fun getFor(id: Int) = getAll {
        FrozenFoodIngredientsTable.itemId eq id
    }?.map { toItem(it).ingredient } ?: throw InvalidAttributeException("FrozenFoodIngredients")

    override suspend fun toItem(row: ResultRow) = FrozenFoodIngredient(
        row[table.id],
        row[table.ingredient],
        row[table.itemId],
        row[table.dateCreated],
        row[table.dateUpdated]
    )

    override fun UpdateBuilder<Int>.toRow(item: FrozenFoodIngredient) {
        this[table.ingredient] = item.ingredient
        this[table.itemId] = item.itemId
    }
}
