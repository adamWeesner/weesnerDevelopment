package breathOfTheWild.images

import generics.IdTable

object ImagesTable : IdTable() {
    val description = text("description")
    val src = text("src")
    val width = integer("width")
    val height = integer("height")
}
