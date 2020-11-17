package breathOfTheWild.effect

import breathOfTheWild.image.ImagesTable
import generics.IdTable

object EffectsTable : IdTable() {
    val name = varchar("name", 255)
    val image = reference("imageId", ImagesTable.id)
    val description = varchar("description", 255)
    val timeLimit = varchar("timeLimit", 255)
}
