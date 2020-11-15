package breathOfTheWild.elixirs

import breathOfTheWild.images.ImagesTable
import generics.IdTable

object ElixirsTable : IdTable() {
    val name = varchar("name", 255)
    val imageId = reference("imageId", ImagesTable.id)
    val effect = varchar("effect", 255)
    val description = text("description")
}
