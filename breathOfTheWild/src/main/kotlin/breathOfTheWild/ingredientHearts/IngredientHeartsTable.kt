package breathOfTheWild.ingredientHearts

import breathOfTheWild.image.ImagesTable
import breathOfTheWild.ingredient.IngredientsTable
import generics.IdTable
import org.jetbrains.exposed.sql.ReferenceOption

object IngredientHeartsTable : IdTable() {
    val imageId = reference("imageId", ImagesTable.id, ReferenceOption.CASCADE)
    val itemId = reference("ingredient", IngredientsTable.id, ReferenceOption.CASCADE)
}
