package breathOfTheWild.ingredient

import breathOfTheWild.image.ImagesTable
import generics.IdTable

object IngredientsTable : IdTable() {
    val title = varchar("title", 255)
    val subtitle = varchar("subtitle", 255).nullable()
    val name = varchar("name", 255)
    val image = reference("imageId", ImagesTable.id)
    val effects = varchar("effects", 255).nullable()
    val bonus = varchar("bonus", 255).nullable()
}
