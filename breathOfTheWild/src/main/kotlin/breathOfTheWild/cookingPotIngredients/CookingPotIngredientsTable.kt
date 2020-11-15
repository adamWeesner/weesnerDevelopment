package breathOfTheWild.cookingPotIngredients

import breathOfTheWild.cookingPotFood.CookingPotFoodsTable
import generics.IdTable
import org.jetbrains.exposed.sql.ReferenceOption

object CookingPotIngredientsTable : IdTable() {
    val name = varchar("name", 255)
    val cookingPotItemId = reference("itemId", CookingPotFoodsTable.id, ReferenceOption.CASCADE)
}
