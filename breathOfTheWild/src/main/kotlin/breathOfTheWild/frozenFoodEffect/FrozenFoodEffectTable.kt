package breathOfTheWild.frozenFoodEffect

import breathOfTheWild.frozenFood.FrozenFoodsTable
import breathOfTheWild.image.ImagesTable
import generics.IdTable
import org.jetbrains.exposed.sql.ReferenceOption

object FrozenFoodEffectTable : IdTable() {
    val imageId = reference("imageId", ImagesTable.id, ReferenceOption.CASCADE)
    val itemId = reference("frozenFood", FrozenFoodsTable.id, ReferenceOption.CASCADE)
}
