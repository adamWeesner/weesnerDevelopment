package breathOfTheWild.frozenFoodIngredients

import breathOfTheWild.frozenFood.FrozenFoodsTable
import generics.IdTable
import org.jetbrains.exposed.sql.ReferenceOption

object FrozenFoodIngredientsTable : IdTable() {
    val ingredient = varchar("ingredient", 255)
    val itemId = reference("frozenFood", FrozenFoodsTable.id, ReferenceOption.CASCADE)
}
