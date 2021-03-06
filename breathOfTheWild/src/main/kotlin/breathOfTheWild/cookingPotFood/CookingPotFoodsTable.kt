package breathOfTheWild.cookingPotFood

import breathOfTheWild.image.ImagesTable
import generics.IdTable

object CookingPotFoodsTable : IdTable() {
    val name = varchar("name", 255)
    val image = reference("imageId", ImagesTable.id)
    val description = varchar("description", 255)
}
