package breathOfTheWild.otherFoodIngredients

import breathOfTheWild.otherFood.OtherFoodsTable
import generics.IdTable
import org.jetbrains.exposed.sql.ReferenceOption

object OtherFoodIngredientsTable : IdTable() {
    val ingredient = varchar("ingredient", 255)
    val itemId = reference("otherFood", OtherFoodsTable.id, ReferenceOption.CASCADE)
}
