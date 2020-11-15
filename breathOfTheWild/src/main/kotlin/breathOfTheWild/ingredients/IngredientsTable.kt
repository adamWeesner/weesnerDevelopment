package breathOfTheWild.ingredients

import generics.IdTable

object IngredientsTable : IdTable() {
    val title = varchar("title", 255)
    val subtitle = varchar("subtitle", 255).nullable()
    val name = varchar("name", 255)
    val effects = text("effects").nullable()
    val bonus = text("bonus").nullable()
}
