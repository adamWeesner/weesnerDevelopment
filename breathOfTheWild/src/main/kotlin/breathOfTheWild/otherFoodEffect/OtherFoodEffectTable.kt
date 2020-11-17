package breathOfTheWild.otherFoodEffect

import breathOfTheWild.image.ImagesTable
import breathOfTheWild.otherFood.OtherFoodsTable
import generics.IdTable
import org.jetbrains.exposed.sql.ReferenceOption

object OtherFoodEffectTable : IdTable() {
    val imageId = reference("imageId", ImagesTable.id, ReferenceOption.CASCADE)
    val itemId = reference("otherFood", OtherFoodsTable.id, ReferenceOption.CASCADE)
}
