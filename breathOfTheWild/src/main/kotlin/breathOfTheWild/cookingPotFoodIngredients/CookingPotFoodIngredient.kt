package breathOfTheWild.cookingPotFoodIngredients

import com.weesnerdevelopment.shared.base.GenericItem
import com.weesnerdevelopment.shared.currentTimeMillis

data class CookingPotFoodIngredient(
    override val id: Int?,
    val ingredient: String,
    val itemId: Int,
    override val dateCreated: Long = currentTimeMillis(),
    override val dateUpdated: Long = currentTimeMillis()
) : GenericItem
