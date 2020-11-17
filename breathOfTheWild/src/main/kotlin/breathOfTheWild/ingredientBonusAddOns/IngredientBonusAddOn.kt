package breathOfTheWild.ingredientBonusAddOns

import shared.base.GenericItem
import shared.currentTimeMillis

data class IngredientBonusAddOn(
    override val id: Int?,
    val imageId: Int,
    val itemId: Int,
    override val dateCreated: Long = currentTimeMillis(),
    override val dateUpdated: Long = currentTimeMillis()
) : GenericItem
