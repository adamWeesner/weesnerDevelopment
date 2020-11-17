package breathOfTheWild.elixirIngredients

import breathOfTheWild.elixir.ElixirsTable
import generics.IdTable
import org.jetbrains.exposed.sql.ReferenceOption

object ElixirIngredientsTable : IdTable() {
    val ingredient = varchar("ingredient", 255)
    val itemId = reference("elixir", ElixirsTable.id, ReferenceOption.CASCADE)
}
