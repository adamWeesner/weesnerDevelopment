package breathOfTheWild.cookingPotFoodIngredients

import breathOfTheWild.cookingPotFood.CookingPotFoodsTable
import generics.IdTable
import org.jetbrains.exposed.sql.ReferenceOption

object CookingPotFoodIngredientsTable : IdTable() {
    val ingredient = varchar("ingredient", 255)
    val itemId = reference("cookingPotFood", CookingPotFoodsTable.id, ReferenceOption.CASCADE)
}
