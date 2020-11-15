package breathOfTheWild.elixirIngredients

import breathOfTheWild.elixirs.ElixirsTable
import generics.IdTable
import org.jetbrains.exposed.sql.ReferenceOption

object ElixirIngredientsTable : IdTable() {
    val name = varchar("name", 255)
    val elixirId = reference("itemId", ElixirsTable.id, ReferenceOption.CASCADE)
}
