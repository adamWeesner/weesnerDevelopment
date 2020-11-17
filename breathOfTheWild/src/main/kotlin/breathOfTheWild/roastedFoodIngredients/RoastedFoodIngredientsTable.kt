package breathOfTheWild.roastedFoodIngredients

import breathOfTheWild.roastedFood.RoastedFoodsTable
import generics.IdTable
import org.jetbrains.exposed.sql.ReferenceOption

object RoastedFoodIngredientsTable : IdTable() {
    val ingredient = varchar("ingredient", 255)
    val itemId = reference("roastedFood", RoastedFoodsTable.id, ReferenceOption.CASCADE)
}
