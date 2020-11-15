package breathOfTheWild.ingredientsHearts

import shared.base.GenericItem
import shared.currentTimeMillis

data class IngredientHearts(
    override val id: Int?,
    val imageId: Int,
    val ingredientId: Int,
    override val dateCreated: Long = currentTimeMillis(),
    override val dateUpdated: Long = currentTimeMillis()
) : GenericItem
