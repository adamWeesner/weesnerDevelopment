package breathOfTheWild.otherFood

import breathOfTheWild.image.ImagesTable
import generics.IdTable

object OtherFoodsTable : IdTable() {
    val name = varchar("name", 255)
    val image = reference("imageId", ImagesTable.id)
    val description = varchar("description", 255)
    val method = varchar("method", 255)
}
