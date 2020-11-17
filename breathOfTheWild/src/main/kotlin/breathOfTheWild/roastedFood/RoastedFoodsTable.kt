package breathOfTheWild.roastedFood

import breathOfTheWild.image.ImagesTable
import generics.IdTable

object RoastedFoodsTable : IdTable() {
    val name = varchar("name", 255)
    val image = reference("imageId", ImagesTable.id)
    val description = varchar("description", 255)
}
