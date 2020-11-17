package breathOfTheWild.roastedFoodEffect

import breathOfTheWild.image.ImagesTable
import breathOfTheWild.roastedFood.RoastedFoodsTable
import generics.IdTable
import org.jetbrains.exposed.sql.ReferenceOption

object RoastedFoodEffectTable : IdTable() {
    val imageId = reference("imageId", ImagesTable.id, ReferenceOption.CASCADE)
    val itemId = reference("roastedFood", RoastedFoodsTable.id, ReferenceOption.CASCADE)
}
