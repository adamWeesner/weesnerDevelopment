package breathOfTheWild.ingredientsBonusAddOns

import breathOfTheWild.images.ImagesTable
import breathOfTheWild.ingredients.IngredientsTable
import generics.IdTable
import org.jetbrains.exposed.sql.ReferenceOption

object BonusAddOnsTable : IdTable() {
    val imageId = reference("imageId", ImagesTable.id, ReferenceOption.CASCADE)
    val ingredientId = reference("itemId", IngredientsTable.id, ReferenceOption.CASCADE)
}
