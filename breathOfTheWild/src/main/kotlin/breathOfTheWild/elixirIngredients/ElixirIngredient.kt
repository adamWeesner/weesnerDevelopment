package breathOfTheWild.elixirIngredients

import shared.base.GenericItem
import shared.currentTimeMillis

data class ElixirIngredient(
    override val id: Int?,
    val ingredient: String,
    val itemId: Int,
    override val dateCreated: Long = currentTimeMillis(),
    override val dateUpdated: Long = currentTimeMillis()
) : GenericItem
