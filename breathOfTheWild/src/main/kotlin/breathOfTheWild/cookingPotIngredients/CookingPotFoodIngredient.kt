package breathOfTheWild.cookingPotIngredients

import shared.base.GenericItem
import shared.currentTimeMillis

data class CookingPotFoodIngredient(
    override val id: Int?,
    val name: String,
    val cookingPotItemId: Int,
    override val dateCreated: Long = currentTimeMillis(),
    override val dateUpdated: Long = currentTimeMillis()
) : GenericItem
