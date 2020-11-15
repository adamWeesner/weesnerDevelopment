package breathOfTheWild.ingredientsDuration

import shared.base.GenericItem
import shared.currentTimeMillis

data class IngredientDuration(
    override val id: Int?,
    val duration: String,
    val ingredientId: Int,
    override val dateCreated: Long = currentTimeMillis(),
    override val dateUpdated: Long = currentTimeMillis()
) : GenericItem
