package breathOfTheWild.ingredientsBonusAddOns

import shared.base.GenericItem
import shared.currentTimeMillis

data class BonusAddOn(
    override val id: Int?,
    val imageId: Int,
    val ingredientId: Int,
    override val dateCreated: Long = currentTimeMillis(),
    override val dateUpdated: Long = currentTimeMillis()
) : GenericItem
