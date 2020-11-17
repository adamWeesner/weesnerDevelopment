package breathOfTheWild.elixir

import breathOfTheWild.image.ImagesTable
import generics.IdTable

object ElixirsTable : IdTable() {
    val name = varchar("name", 255)
    val image = reference("imageId", ImagesTable.id)
    val effect = varchar("effect", 255)
    val description = varchar("description", 255)
}
