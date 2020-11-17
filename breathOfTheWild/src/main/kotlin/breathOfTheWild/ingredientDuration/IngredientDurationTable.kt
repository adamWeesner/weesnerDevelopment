package breathOfTheWild.ingredientDuration

import breathOfTheWild.ingredient.IngredientsTable
import generics.IdTable
import org.jetbrains.exposed.sql.ReferenceOption

object IngredientDurationTable : IdTable() {
    val duration = varchar("duration", 255)
    val itemId = reference("ingredient", IngredientsTable.id, ReferenceOption.CASCADE)
}
