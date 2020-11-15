package breathOfTheWild.ingredientsDuration

import breathOfTheWild.ingredients.IngredientsTable
import generics.IdTable
import org.jetbrains.exposed.sql.ReferenceOption

object IngredientDurationsTable : IdTable() {
    val duration = text("duration")
    val ingredientId = reference("itemId", IngredientsTable.id, ReferenceOption.CASCADE)
}
