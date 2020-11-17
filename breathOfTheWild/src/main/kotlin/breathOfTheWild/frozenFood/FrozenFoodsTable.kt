package breathOfTheWild.frozenFood

import breathOfTheWild.image.ImagesTable
import generics.IdTable

object FrozenFoodsTable : IdTable() {
    val name = varchar("name", 255)
    val image = reference("imageId", ImagesTable.id)
    val description = varchar("description", 255)
}
