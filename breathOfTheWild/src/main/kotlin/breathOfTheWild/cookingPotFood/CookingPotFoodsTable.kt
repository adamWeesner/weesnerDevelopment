package breathOfTheWild.cookingPotFood

import breathOfTheWild.images.ImagesTable
import generics.IdTable

object CookingPotFoodsTable : IdTable() {
    val name = varchar("name", 255)
    val imageId = reference("imageId", ImagesTable.id)
    val description = varchar("description", 255)
}
