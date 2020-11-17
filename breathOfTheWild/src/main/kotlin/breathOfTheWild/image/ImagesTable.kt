package breathOfTheWild.image

import generics.IdTable

object ImagesTable : IdTable() {
    val description = varchar("description", 255)
    val src = varchar("src", 255)
    val width = integer("width")
    val height = integer("height")
}
